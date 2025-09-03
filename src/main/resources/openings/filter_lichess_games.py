import chess.pgn
from collections import defaultdict
from tqdm import tqdm

class OpeningTrieNode:
    def __init__(self):
        self.count = 0
        self.children = defaultdict(OpeningTrieNode)

def add_line_to_trie(root, line):
    node = root
    for move in line:
        node = node.children[move]
        node.count += 1

def extract_maximal_lines(root, min_frequency):
    """
    Extrahiert alle maximalen Opening-Zweige aus dem Trie.
    Ein Pfad wird nur gespeichert, wenn er nicht weiter verlängerbar ist,
    d.h. keine Erweiterung mehr existiert, die ebenfalls mindestens
    min_frequency Mal auftritt.
    """
    result = []
    
    def dfs(node, path):
        # Ermittle alle Kinder, die die Mindesthäufigkeit haben
        valid_children = [(move, child) for move, child in node.children.items() if child.count >= min_frequency]
        # Falls keine gültigen Erweiterungen existieren und der Pfad nicht leer ist,
        # gilt der aktuelle Pfad als maximal.
        if not valid_children and path:
            result.append(list(path))
        else:
            # Ansonsten weiter in die Kinder verzweigen
            for move, child in valid_children:
                path.append(move)
                dfs(child, path)
                path.pop()
    
    dfs(root, [])
    return result

def process_pgn_build_frequent_openings(pgn_path, output_path, min_frequency=10):
    root = OpeningTrieNode()
    total_games = 0
    longest_opening = []  # Aktuell längster gültiger Opening-Zweig

    with open(pgn_path, 'r', encoding='utf-8') as pgn, \
         tqdm(desc="Verarbeite Games", unit="Spiel", dynamic_ncols=True) as pbar:
        
        while True:
            game = chess.pgn.read_game(pgn)
            if game is None:
                break

            board = game.board()
            game_line = []
            for move in game.mainline_moves():
                try:
                    san = board.san(move)
                    game_line.append(san)
                    board.push(move)
                except Exception:
                    break

            if len(game_line) >= 2:
                add_line_to_trie(root, game_line)
                total_games += 1

                # Ermittle aus der aktuellen Spielreihe den "gültigen" Präfix, der häufig ist
                node = root
                valid_prefix = []
                for move in game_line:
                    if move in node.children and node.children[move].count >= min_frequency:
                        node = node.children[move]
                        valid_prefix.append(move)
                    else:
                        break

                if len(valid_prefix) > len(longest_opening):
                    longest_opening = valid_prefix.copy()

                if total_games % 1000 == 0:
                    pbar.set_postfix(Length=len(longest_opening))
            
            pbar.update(1)

    # Extrahiere aus dem Trie nur die maximalen, häufigen Opening-Zweige.
    maximal_lines = extract_maximal_lines(root, min_frequency)
    seen = set()

    with open(output_path, "w", encoding="utf-8") as out:
        for line in maximal_lines:
            key = " ".join(line)
            if key not in seen:
                seen.add(key)
                out.write(key + "\n")

    print(f"\n✅ {len(seen)} eindeutige Opening-Zweige gespeichert in: {output_path}")
    print(f"📏 Aktuell längster Opening-Zweig: {' '.join(longest_opening)} ({len(longest_opening)} Züge)")

# Beispielaufruf
process_pgn_build_frequent_openings(
    pgn_path="lichess_elite_2023-07.pgn",
    output_path="lichess_elite_2023-07_openings.txt",
    min_frequency=100
)
