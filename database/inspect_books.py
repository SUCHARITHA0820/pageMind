import glob
import importlib.util
import os
import re

def inspect_all():
    generators = sorted(glob.glob(r"c:\pdd1\database\generate_*.py"))
    total_books = 0
    for gen in generators:
        with open(gen, 'r', encoding='utf-8') as f:
            content = f.read()
        # Look for tuples like ("...", "...", "...", "...") or ('...', '...', '...', '...')
        tuples = re.findall(r'\(\s*["\']([^"\']+)["\']\s*,\s*["\']([^"\']+)["\']\s*,\s*["\']([^"\']*)["\']\s*,\s*["\']([^"\']*)["\']\s*\)', content)
        print(f"{os.path.basename(gen)}: {len(tuples)} books found via regex")
        total_books += len(tuples)
    print(f"Total across all generators: {total_books}")

if __name__ == "__main__":
    inspect_all()
