import re
from collections import defaultdict
from tqdm import tqdm

class OpeningTrieNode:
    # __slots__ reduziert den Speicherbedarf massiv, da der Trie sehr groß werden kann
    __slots__ = ['count', 'children']

    def __init__(self):
        self.count = 0
        self.children = defaultdict(OpeningTrieNode)

def add_line_to_trie(root, line, max_depth):
    """Fügt einen Zweig dem Trie hinzu, beschränkt auf max_depth Halbzuege."""
    node = root
    for i, move in enumerate(line):
        if i >= max_depth:
            break
        node = node.children[move]
        node.count += 1

def extract_maximal_lines(node, min_freq, min_moves, path=None, result=None):
    """
    Sucht rekursiv nach den tiefsten (maximalen) Zweigen, die mindestens
    min_freq Mal vorkommen und mindestens min_moves lang sind.
    """
    if path is None:
        path = []
    if result is None:
        result = []

    # Finde alle Erweiterungen, die die Häufigkeits-Bedingung erfüllen
    valid_children = [(m, c) for m, c in node.children.items() if c.count >= min_freq]

    # Wenn der Zweig hier nicht mehr oft genug fortgesetzt wird:
    if not valid_children:
        if len(path) >= min_moves:
            result.append(list(path))
    else:
        # Sonst weiter in die Tiefe gehen
        for m, c in valid_children:
            path.append(m)
            extract_maximal_lines(c, min_freq, min_moves, path, result)
            path.pop()

    return result

def process_pgn(pgn_path, output_path, min_freq=100, min_moves=6, max_depth=60, max_games=None):
    """
    Liest eine .pgn Datei, baut einen Trie auf und speichert die gefilterten Openings.

    :param pgn_path: Pfad zur PGN-Datei
    :param output_path: Pfad zur Ausgabe-Textdatei
    :param min_freq: Mindestanzahl, wie oft das Opening gespielt worden sein muss
    :param min_moves: Mindestanzahl an Halbzügen für das Opening (z.B. 6 = 3 ganze Züge)
    :param max_depth: Maximale Tiefe (Halbzüge), die in den Trie geladen wird (Spart Speicher)
    :param max_games: Optional. Maximale Anzahl an Spielen, die verarbeitet werden sollen
    """

    print("🔍 Schritt 1: Zähle Spiele für die Fortschrittsanzeige...")
    total_games = 0
    with open(pgn_path, 'r', encoding='utf-8') as f:
        for line in f:
            if line.startswith('[Event '):
                total_games += 1
                if max_games and total_games >= max_games:
                    break

    print(f"✅ {total_games} Spiele gefunden.\n")

    root = OpeningTrieNode()

    # Regex, um Zugnummern wie "1." oder "23..." effizient zu entfernen
    move_num_pattern = re.compile(r'\b\d+\.+')
    # End-Marker, die signalisieren, dass ein Spiel vorbei ist
    end_markers = {"1-0", "0-1", "1/2-1/2", "*"}

    processed_games = 0

    print("🌳 Schritt 2: Verarbeite PGN und baue Trie auf...")
    with open(pgn_path, 'r', encoding='utf-8') as f, \
         tqdm(total=total_games, unit="Spiel", dynamic_ncols=True) as pbar:

        current_game_moves = []

        for line in f:
            # Header und leere Zeilen überspringen
            if line.startswith('[') or not line.strip():
                continue

            # Zugnummern entfernen und in Tokens splitten
            clean_line = move_num_pattern.sub('', line)
            tokens = clean_line.split()

            for token in tokens:
                if token in end_markers:
                    if current_game_moves:
                        add_line_to_trie(root, current_game_moves, max_depth)
                        current_game_moves = []
                        processed_games += 1
                        pbar.update(1)

                        if max_games and processed_games >= max_games:
                            break
                else:
                    current_game_moves.append(token)

            if max_games and processed_games >= max_games:
                break

        # Falls die Datei nicht sauber mit einem End-Marker endet, den letzten Rest sichern
        if current_game_moves and (not max_games or processed_games < max_games):
            add_line_to_trie(root, current_game_moves, max_depth)
            processed_games += 1
            pbar.update(1)

    print("\n⚙️ Schritt 3: Extrahiere und filtere maximale Opening-Zweige...")
    maximal_lines = extract_maximal_lines(root, min_freq=min_freq, min_moves=min_moves)

    print(f"💾 Schritt 4: Speichere Ergebnisse in '{output_path}'...")
    with open(output_path, "w", encoding="utf-8") as out:
        out.write("\n".join(" ".join(line) for line in maximal_lines))

    longest_opening = max(maximal_lines, key=len) if maximal_lines else []

    print(f"\n✅ Erfolgreich abgeschlossen!")
    print(f"📁 {len(maximal_lines)} eindeutige Opening-Zweige gespeichert.")
    if longest_opening:
        print(f"📏 Aktuell längster Opening-Zweig: {' '.join(longest_opening)} ({len(longest_opening)} Züge)")


if __name__ == "__main__":
    # Beispielaufruf
    process_pgn(
        pgn_path="lichess_elite_2021-10.pgn",
        output_path="lichess_elite_2021-10_openings.txt",
        min_freq=100,        # Opening muss mindestens 100 Mal vorkommen
        min_moves=6,         # Das Opening muss mindestens 6 Halbzüge lang sein
        max_depth=40,        # Wir schauen uns nur die ersten 30 vollen Züge (60 Halbzüge) jedes Spiels an
        max_games=None       # None = ganze Datei. Setze z.B. 10000, um das Skript vorher zu stoppen
    )