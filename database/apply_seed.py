import pymysql

conn = pymysql.connect(host='localhost', user='root', password='Suchi@520', database='pagemind')
cursor = conn.cursor()
cursor.execute("SET FOREIGN_KEY_CHECKS = 0;")

with open('c:/pdd1/database/seed_books.sql', 'r', encoding='utf-8') as f:
    sql_text = f.read()

statements = sql_text.split(';')
count = 0
for s in statements:
    s = s.strip()
    if s and not s.startswith('--'):
        cursor.execute(s)
        if s.upper().startswith("INSERT INTO"):
            count += 1

conn.commit()
conn.close()
print(f"Successfully applied seed_books.sql! Total books inserted: {count}")
