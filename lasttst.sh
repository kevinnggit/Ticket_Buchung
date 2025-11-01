#!/bin/bash
# Konfiguration
baseurl="https://informatik.hs-bremerhaven.de"
webapp="docker-kevnguefackdjoukeng-java"
path="$baseurl/$webapp"

mkdir -p tmp

# Function du workflow de chaque utilisateur
runWorkflow() {
    local i="$1"
    local username="user${i}"
    local email="${username}@example.com"
    local password="pwd${i}"
    local cookieFile="tmp/cookie-${i}.jar"

    # Registrierung
    curl -s -L -c "$cookieFile" -b "$cookieFile" -d "benutzername=${username}&email=${email}&passwort=${password}&name=Nachname${i}&vorname=Vorname${i}&adresse=Adresse${i}&postleitzahl=12345&stadt=Stadt${i}" "$path/register" >/dev/null

    # Login
    curl -s -L -c "$cookieFile" -b "$cookieFile" -d "benutzername=${username}&passwort=${password}" "$path/login" >/dev/null

    # Buchung: 
    curl -s -L -c "$cookieFile" -b "$cookieFile" -d "center=TestCenter&date1=2025-03-31&heure1=10:00&vaccine=1&familien_mitglied=&relation=" "$path/buchung" >/dev/null

    # Logout
    curl -s -L -c "$cookieFile" -b "$cookieFile" "$path/logout" >/dev/null

    # suprime le cookie
    rm -f "$cookieFile"
}

total=5000
concurrency=100

# Debut
start=$(date +%s)

for (( i=1; i<=total; i++ )); do
    runWorkflow "$i" &
    if (( i % concurrency == 0 )); then
        wait
    fi
done
wait

# Fin
end=$(date +%s)
elapsed=$((end - start))

# calcul le temp ecoulé
elapsed_minutes=$(echo "scale=2; $elapsed/60" | bc)
# Berechne Anfragen pro Minute
qpm=$(echo "scale=2; $total/$elapsed_minutes" | bc)
# compte les envoies pas seconde
qps=$(echo "scale=2; $total/$elapsed" | bc)

echo "Total Workflow-Anfragen: $total"
echo "Verstrichene Zeit: $elapsed Sekunden (~$elapsed_minutes Minuten)"
echo "Anfragen pro Minute: $qpm"
echo "Anfragen pro Sekunde: $qps"

