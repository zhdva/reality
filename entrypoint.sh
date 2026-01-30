#!/bin/sh
set -e

tailscaled --tun=userspace-networking --state=/var/lib/tailscale/tailscaled.state --verbose=1 2>&1 & TAILSCALED_PID=$!
echo "Tailscaled запущен с PID: $TAILSCALED_PID"

sleep 5

if ! kill -0 $TAILSCALED_PID 2>/dev/null; then
    echo "ERROR: tailscaled завершился!"
    exit 1
fi

tailscale up \
  --auth-key=${TAILSCALE_AUTHKEY} \
  --hostname=reality \
  --accept-dns=false \
  --advertise-exit-node

exec java -jar /reality/reality.jar