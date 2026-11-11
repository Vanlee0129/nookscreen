#!/usr/bin/env bash
set -euo pipefail

repo=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)
output=${1:?Usage: tools/build-website.sh OUTPUT_DIRECTORY}

mkdir -p -- "$output/en" "$output/assets"
cp -- "$repo/website/index.html" "$repo/website/style.css" "$output/"
cp -- "$repo/website/en/index.html" "$output/en/"
cp -- "$repo/docs/brand/icon.png" "$output/assets/"
for name in sanrio-home sanrio-scene sanrio-theme marvel-theme; do
    cp -- "$repo/docs/screenshots/$name.jpg" "$output/assets/"
done
