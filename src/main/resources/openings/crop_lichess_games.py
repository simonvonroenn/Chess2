import chess.pgn
from tqdm import tqdm

def extract_first_n_games(input_pgn, output_pgn, n=1000):
    count = 0
    with open(input_pgn, 'r', encoding='utf-8') as infile, \
         open(output_pgn, 'w', encoding='utf-8') as outfile, \
         tqdm(desc="Extrahiere Spiele", total=n, unit="Spiel", dynamic_ncols=True) as pbar:
        
        while count < n:
            game = chess.pgn.read_game(infile)
            if game is None:
                break  # keine weiteren Spiele vorhanden
            # Schreibe das Spiel in die neue Datei
            outfile.write(str(game))
            outfile.write("\n\n")
            count += 1
            pbar.update(1)
    
    print(f"\n✅ {count} Spiele in {output_pgn} gespeichert.")

# Beispielaufruf:
extract_first_n_games("lichess_elite_2023-07.pgn", "lichess_elite_2023-07_10000.pgn", n=10000)
