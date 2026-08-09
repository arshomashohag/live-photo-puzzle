#!/usr/bin/env bash
set -euo pipefail
OUT="app/src/main/res/drawable-nodpi"
mkdir -p "$OUT"

# name:seed pairs — seeds pinned for reproducibility.
# Images come from picsum.photos (Unsplash, free to use). Fetched at dev time
# only; the shipped app reads the committed JPEGs and never touches the network.
IMAGES=(
  "tessera_easy_1:ridgeline"
  "tessera_easy_2:harbour"
  "tessera_easy_3:terrace"
  "tessera_medium_1:meridian"
  "tessera_medium_2:lattice"
  "tessera_medium_3:quartz"
  "tessera_hard_1:cordon"
  "tessera_hard_2:bastion"
  "tessera_hard_3:cascade"
)

resize() { # $1 = file
  if command -v sips >/dev/null 2>&1; then
    sips -Z 1024 "$1" >/dev/null
  elif command -v convert >/dev/null 2>&1; then
    convert "$1" -resize 1024x1024^ -gravity center -extent 1024x1024 -quality 80 "$1"
  fi
}

for pair in "${IMAGES[@]}"; do
  name="${pair%%:*}"; seed="${pair##*:}"
  echo "Fetching $name (seed=$seed)"
  curl -sL "https://picsum.photos/seed/${seed}/1024/1024.jpg" -o "$OUT/${name}.jpg"
  resize "$OUT/${name}.jpg"
done
echo "Done. Files in $OUT"
