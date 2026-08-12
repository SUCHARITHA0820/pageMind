import glob
import importlib.util
import os

def test_imports():
    generators = sorted(glob.glob(r"c:\pdd1\database\generate_*.py"))
    total = 0
    for gen in generators:
        spec = importlib.util.spec_from_file_location("module", gen)
        mod = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(mod)
        
        # Check dict names
        data_dict = None
        for name in ['GENRE_DATA', 'GENRE_DATA_BATCH_2', 'GENRE_DATA_BATCH_3', 'GENRE_DATA_BATCH_4', 
                     'GENRE_DATA_BATCH_5', 'GENRE_DATA_BATCH_6', 'GENRE_DATA_BATCH_7', 'GENRE_DATA_BATCH_8',
                     'GENRE_DATA_BATCH_9', 'genres_data']:
            if hasattr(mod, name):
                data_dict = getattr(mod, name)
                break
        
        if data_dict:
            count = sum(len(books) for books in data_dict.values())
            print(f"{os.path.basename(gen)}: {count} books in dict across {len(data_dict)} genres")
            total += count
        else:
            print(f"FAILED to find data dict in {gen}")
            
    print(f"Total books in dicts: {total}")

if __name__ == "__main__":
    test_imports()
