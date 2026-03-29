import os
from tqdm import tqdm

def merge_opening_files(input_files, output_directory="."):
    """
    Nimmt eine Liste von Textdateien mit Openings, fügt sie zusammen,
    entfernt Duplikate und speichert sie in einer neuen Datei.
    """
    unique_openings = set()

    print(f"🔄 Starte das Mergen von {len(input_files)} Dateien...")

    # Iteriere mit Fortschrittsbalken über die Dateien
    for file_path in tqdm(input_files, desc="Lese Dateien", unit="Datei", dynamic_ncols=True):
        if not os.path.exists(file_path):
            print(f"\n⚠️ Warnung: Datei '{file_path}' nicht gefunden. Wird übersprungen.")
            continue

        with open(file_path, 'r', encoding='utf-8') as f:
            lines = f.read().splitlines()

            # Füge alle nicht-leeren Zeilen dem Set hinzu (Duplikate werden automatisch ignoriert)
            unique_openings.update(line.strip() for line in lines if line.strip())

    print(f"\n✅ Insgesamt {len(unique_openings)} eindeutige Openings gefunden.")

    output_filename = f"openings_db_{len(unique_openings)}.txt"
    output_path = os.path.join(output_directory, output_filename)

    # Optional: Wir sortieren die Liste alphabetisch, damit sie ordentlich aussieht
    sorted_openings = sorted(list(unique_openings))

    print(f"💾 Speichere gemergte Liste in '{output_path}'...")

    with open(output_path, 'w', encoding='utf-8') as out:
        out.write("\n".join(sorted_openings))

    print("🎉 Erfolgreich abgeschlossen!")

if __name__ == "__main__":
    # Beispielaufruf
    dateien_zum_mergen = [
        "lichess_elite_2023-07_openings.txt",
        "lichess_elite_2021-10_openings.txt",
        # Weitere Dateien hier hinzufügen...
    ]

    merge_opening_files(dateien_zum_mergen)