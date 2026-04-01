#!/bin/sh

apk update

. ./scripts/tailscale.sh

./scripts/mtproto.sh

exec java -jar /reality/reality.jar