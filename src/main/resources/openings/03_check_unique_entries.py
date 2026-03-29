import os
from tqdm import tqdm

def verify_unique_openings(file_path):
    """
    Überprüft eine Textdatei darauf, ob jede Zeile (jedes Opening) absolut einzigartig ist.
    """
    if not os.path.exists(file_path):
        print(f"❌ Fehler: Die Datei '{file_path}' existiert nicht.")
        return

    seen_lines = set()
    duplicates = 0
    empty_lines = 0
    total_valid_lines = 0

    print(f"🔍 Überprüfe Datei: '{file_path}'")

    # Zuerst ermitteln wir die Gesamtanzahl der Zeilen für die Fortschrittsanzeige
    with open(file_path, 'r', encoding='utf-8') as f:
        total_file_lines = sum(1 for _ in f)

    # Jetzt lesen wir die Datei aus und prüfen auf Duplikate
    with open(file_path, 'r', encoding='utf-8') as f, \
         tqdm(total=total_file_lines, desc="Prüfe Zeilen", unit="Zeile", dynamic_ncols=True) as pbar:

        for line in f:
            # Zeilenumbruch und unsichtbare Leerzeichen am Rand entfernen
            clean_line = line.strip()

            pbar.update(1)

            if not clean_line:
                empty_lines += 1
                continue

            total_valid_lines += 1

            # Der eigentliche Check: Haben wir diese Zeile schon gesehen?
            if clean_line in seen_lines:
                duplicates += 1
            else:
                seen_lines.add(clean_line)

    # Ausgabe der Ergebnisse
    print("\n" + "="*40)
    print("📊 ERGEBNIS DER ÜBERPRÜFUNG")
    print("="*40)
    print(f"🔹 Gesamte Textzeilen in Datei: {total_file_lines}")
    if empty_lines > 0:
        print(f"⚠️ Leere Zeilen ignoriert:      {empty_lines}")
    print(f"🔹 Verwertbare Openings:        {total_valid_lines}")
    print(f"🔹 Eindeutige Openings (Set):   {len(seen_lines)}")
    print("-" * 40)

    if duplicates == 0:
        print("✅ HERVORRAGEND! Jede Zeile in deiner Datenbank ist zu 100% einzigartig.")
    else:
        print(f"❌ FEHLER: Es wurden {duplicates} doppelte Einträge gefunden!")

if __name__ == "__main__":
    # Trage hier den Namen deiner Datenbank ein, die du prüfen möchtest
    datenbank_datei = "openings_db_3624.txt"

    verify_unique_openings(datenbank_datei)