import sqlite3
import time
from bricklink import BricklinkAPI

# Bricklink API credentials
CONSUMER_KEY = ""
CONSUMER_SECRET = ""
TOKEN_VALUE = ""
TOKEN_SECRET = ""



#updating all the prices
def update_set_prices(db_path='lego_sets.db', max_api_calls=4800):

    #Create the briclink API client
    client = BricklinkAPI(
        consumer_key=CONSUMER_KEY,
        consumer_secret=CONSUMER_SECRET,
        token_value=TOKEN_VALUE,
        token_secret=TOKEN_SECRET
    )
    
    try:
        #Connect to the database
        conn = sqlite3.connect(db_path)
        cursor = conn.cursor()
        
        #Grab all sets without prices
        cursor.execute("""
            SELECT set_number, name 
            FROM sets 
            WHERE set_number IS NOT NULL 
            AND set_number != '' 
            AND new_price IS NULL 
            AND used_price IS NULL
        """)
        sets = cursor.fetchall()
        
        total_sets = len(sets)
        max_sets = max_api_calls // 2
        sets_to_process = min(total_sets, max_sets)
        
        print(f"Found {total_sets} sets without prices")

        
        updated_count = 0
        error_count = 0
        api_call_count = 0
        
        #api call limit
        for idx, (set_number, name) in enumerate(sets, 1):
           
            if api_call_count >= max_api_calls:
                print("="*60)
                print(f"Reached API call limit ({max_api_calls})")
                print(f"Processed {updated_count} sets successfully")
                break
            
            #needs the -1 cause bricklink uses this
            api_set_num = set_number if set_number.endswith('-1') else f"{set_number}-1"
            
            print(f"[{idx}/{sets_to_process}] Processing {api_set_num} - {name}")
            print(f"  API calls used: {api_call_count}/{max_api_calls}")
            
            try:
                #getting the new price
                new_price = None
                try:
                    new_data = client.get_set_price_guide(
                        api_set_num,
                        guide_type="sold",
                        new_or_used="N",
                        country_code="US",
                        currency_code="USD"
                    )
                    api_call_count += 1
                    if new_data.get('data') and new_data['data'].get('avg_price'):
                        new_price = float(new_data['data']['avg_price'])
                        print(f"  NEW price: ${new_price:.2f}")
                
                #typically fails when no data is found, doesnt exist
                except Exception as e:
                    print(f"Failed to fetch NEW price: {e}")
                    api_call_count += 1
                
                #adding a delay
                time.sleep(0.5)
                
                #getting the used price
                used_price = None
                try:
                    used_data = client.get_set_price_guide(
                        api_set_num,
                        guide_type="sold",
                        new_or_used="U",
                        country_code="US",
                        currency_code="USD"
                    )
                    
                    #adding to count
                    api_call_count += 1
                    if used_data.get('data') and used_data['data'].get('avg_price'):
                        used_price = float(used_data['data']['avg_price'])
                        print(f"USED price: ${used_price:.2f}")
                        
                        
                except Exception as e:
                    print(f"Not able to fetch USED price: {e}")
                    api_call_count += 1
                
                #updating the database
                cursor.execute("""
                    UPDATE sets 
                    SET new_price = ?, used_price = ? 
                    WHERE set_number = ?
                """, (new_price, used_price, set_number))
                
                updated_count += 1
                
                #we want to commit every 10 cause idk when crash
                if updated_count % 10 == 0:
                    conn.commit()
                    print(f"  Committed {updated_count} updates")
                
                #limit the processing
                time.sleep(1)
                
            except Exception as e:
                print(f"  ERROR: {e}")
                error_count += 1
                continue
        
        #final commit
        conn.commit()

        print(f"Errors {error_count}")
        print(f"Total API calls used {api_call_count}/{max_api_calls}")
        print(f"Remaining sets without prices {total_sets - updated_count}")
        
        conn.close()
        
    except Exception as e:
        print(f"Database error: {e}")
        raise

if __name__ == "__main__":
    update_set_prices()