#!/bin/bash
set -e

if [ -z "$MTPROTO_SECRET" ]; then
    echo "ERROR: переменная MTPROTO_SECRET не задана. Прокси не может быть запущен."
    exit 1
fi

command -v python3 >/dev/null 2>&1 || { echo "ERROR: python3 не найден"; exit 1; }
command -v git >/dev/null 2>&1 || { echo "ERROR: git не найден"; exit 1; }

MT_PROXY_DIR=/opt/mtprotoproxy
mkdir -p "$MT_PROXY_DIR"

cd "$MT_PROXY_DIR"
if [ ! -d ".git" ]; then
    git clone https://github.com/alexbers/mtprotoproxy.git .
else
    git pull
fi

TLS_DOMAIN="${CUSTOM_TLS_DOMAIN:-$TS_DOMAIN}"

cat > config.py <<EOF
PORT = 8443
USERS = {
    "default": "$MTPROTO_SECRET"
}
TLS_DOMAIN = "$TLS_DOMAIN"
EOF

if ! pgrep -f "python3.*mtprotoproxy.py" > /dev/null; then
    nohup python3 mtprotoproxy.py > /var/log/mtproto.log 2>&1 &
    echo "==> MTProto Proxy запущен на порту 8443 с доменом $TLS_DOMAIN"
else
    echo "==> MTProto Proxy уже работает. Пропускаем запуск."
fi