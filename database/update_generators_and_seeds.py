import glob
import os
import re
import subprocess

def fix_and_update(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
        
    filename = os.path.basename(filepath)
    
    # Clean up any bad escaped quotes if present
    content = content.replace('f\\"', 'f"').replace(';\\n\\"', ';\\n"')

    # 1. Add import if not present
    if "from book_metadata_utils import get_rating, get_published_year" not in content:
        import_line = "from book_metadata_utils import get_rating, get_published_year\n"
        first_import = re.search(r'^(import|from)\s+', content, re.MULTILINE)
        if first_import:
            pos = first_import.start()
            content = content[:pos] + import_line + content[pos:]
        else:
            content = import_line + content

    # 2. Check and replace SQL statement insertion logic
    if "e_title" in content:
        # Match old or partially updated SQL string
        pattern = r"(\s*)(pub_year = get_published_year[^\n]+\n\s*rating = get_rating[^\n]+\n\s*)?sql = f\\?\"INSERT (IGNORE )?INTO books \(title, author, genre, description, cover_url, buy_links_json(?:, published_year, rating)?\) VALUES \([^)]+\);\\?n\\?\""
        
        def repl_A(m):
            indent = m.group(1)
            ignore = m.group(3) if m.group(3) else ""
            return (
                f"{indent}pub_year = get_published_year(title, author, genre, desc, isbn)\n"
                f"{indent}rating = get_rating(title, author)\n"
                f"{indent}sql = f\"INSERT {ignore}INTO books (title, author, genre, description, cover_url, buy_links_json, published_year, rating) VALUES ('{{e_title}}', '{{e_author}}', '{{e_genre}}', '{{e_desc}}', '{{e_cover_url}}', '{{e_buy_links_json}}', {{pub_year}}, {{rating}});\\n\""
            )
            
        new_content, count = re.subn(pattern, repl_A, content)
    elif "t_esc" in content:
        pattern = r"(\s*)(pub_year = get_published_year[^\n]+\n\s*rating = get_rating[^\n]+\n\s*)?sql = f\\?\"INSERT (IGNORE )?INTO books \(title, author, genre, description, cover_url, buy_links_json(?:, published_year, rating)?\) VALUES \([^)]+\);\\?n\\?\""
        
        def repl_B(m):
            indent = m.group(1)
            ignore = m.group(3) if m.group(3) else ""
            return (
                f"{indent}pub_year = get_published_year(title, author, genre, desc, isbn)\n"
                f"{indent}rating = get_rating(title, author)\n"
                f"{indent}sql = f\"INSERT {ignore}INTO books (title, author, genre, description, cover_url, buy_links_json, published_year, rating) VALUES ('{{t_esc}}', '{{a_esc}}', '{{genre}}', '{{d_esc}}', '{{cover_url}}', '{{buy_json}}', {{pub_year}}, {{rating}});\\n\""
            )
            
        new_content, count = re.subn(pattern, repl_B, content)
    else:
        print(f"UNKNOWN pattern in {filename}")
        return

    if count > 0:
        print(f"SUCCESS: Updated {filename}")
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
    else:
        print(f"WARNING: No pattern match for {filename}")

def main():
    generators = [
        r"c:\pdd1\database\generate_seed.py",
        r"c:\pdd1\database\generate_batch_2.py",
        r"c:\pdd1\database\generate_batch_3.py",
        r"c:\pdd1\database\generate_batch_4.py",
        r"c:\pdd1\database\generate_batch_5.py",
        r"c:\pdd1\database\generate_batch_6.py",
        r"c:\pdd1\database\generate_batch_7.py",
        r"c:\pdd1\database\generate_batch_8.py",
        r"c:\pdd1\database\generate_batch_9.py",
        r"c:\pdd1\database\generate_batch_10.py"
    ]
    
    for gen in generators:
        fix_and_update(gen)
        
    print("\nExecuting generator scripts to produce updated SQL seed files...")
    for gen in generators:
        fname = os.path.basename(gen)
        res = subprocess.run(["python", gen], cwd=r"c:\pdd1\database", capture_output=True, text=True)
        if res.returncode == 0:
            output_msg = res.stdout.strip() if res.stdout else "Success"
            print(f"SUCCESS {fname}: {output_msg}")
        else:
            print(f"ERROR {fname}: {res.stderr}")

if __name__ == "__main__":
    main()
