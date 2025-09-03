import sys
from pathlib import Path
from tqdm import tqdm

def filter_openings(input_path, min_moves):
    input_path = Path(input_path)
    output_path = input_path.with_name(input_path.stem + f"_long.txt")

    total_lines = sum(1 for _ in open(input_path, 'r', encoding='utf-8'))
    kept = 0

    with open(input_path, 'r', encoding='utf-8') as infile, \
         open(output_path, 'w', encoding='utf-8') as outfile:

        for line in tqdm(infile, total=total_lines, desc="Filtere Openings"):
            line = line.strip()
            if not line:
                continue
            moves = line.split()
            if len(moves) >= min_moves:
                outfile.write(line + '\n')
                kept += 1

    print(f"\n✅ {kept} Opening-Zeilen mit ≥ {min_moves} Zügen gespeichert in: {output_path}")

filter_openings("lichess_elite_2023-07_openings_base.txt", 6)