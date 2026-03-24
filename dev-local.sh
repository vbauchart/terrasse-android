#!/usr/bin/env bash
set -euo pipefail

# Détecte l'IP sur le réseau local (interface vers l'extérieur)
LOCAL_IP=$(ip route get 1.1.1.1 | awk '{print $7; exit}')
POCKETBASE_URL="http://$LOCAL_IP:8090"

echo "→ IP locale     : $LOCAL_IP"
echo "→ PocketBase URL: $POCKETBASE_URL"

# Démarrage PocketBase
echo ""
echo "→ Démarrage PocketBase..."
docker compose up -d

# Attente que PocketBase soit prêt
echo "→ Attente PocketBase (max 30s)..."
for i in $(seq 1 30); do
    if curl -sf "http://localhost:8090/api/health" > /dev/null 2>&1; then
        echo "→ PocketBase prêt ✓"
        break
    fi
    if [ "$i" -eq 30 ]; then
        echo "✗ PocketBase ne répond pas après 30s"
        exit 1
    fi
    sleep 1
done

# Injection de l'IP dans local.properties (lu par Android Studio et Gradle)
if grep -q "^pocketbaseUrl=" local.properties 2>/dev/null; then
    sed -i "s|^pocketbaseUrl=.*|pocketbaseUrl=$POCKETBASE_URL|" local.properties
else
    echo "pocketbaseUrl=$POCKETBASE_URL" >> local.properties
fi
echo "→ local.properties mis à jour ✓"
