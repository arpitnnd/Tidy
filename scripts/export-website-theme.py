"""Export the default Tidy palette into the static website stylesheet."""

import re
from pathlib import Path


root = Path(__file__).resolve().parents[1]
source = (root / "app/src/main/java/com/tidy/app/theme/Color.kt").read_text(encoding="utf-8")
colours = dict(re.findall(r"val (Tidy\w+) = Color\(0xFF([0-9A-F]{6})\)", source))
roles = {
    "background": "Background",
    "surface": "SurfaceVariant",
    "text": "OnBackground",
    "muted": "OnSurfaceVariant",
    "accent": "Primary",
    "border": "Outline",
}


def declarations(theme, indent="  "):
    return "\n".join(
        f"{indent}--{role}: #{colours[f'Tidy{theme}{token}'].lower()};"
        for role, token in roles.items()
    )


stylesheet = root / "website/assets/site.css"
css = stylesheet.read_text(encoding="utf-8")
css, light_count = re.subn(
    r":root \{\n  color-scheme: light dark;.*?\n\}",
    ":root {\n  color-scheme: light dark;\n" + declarations("Light") + "\n}",
    css,
    count=1,
    flags=re.S,
)
css, dark_count = re.subn(
    r"@media \(prefers-color-scheme: dark\) \{.*?\n\}",
    "@media (prefers-color-scheme: dark) {\n  :root {\n"
    + declarations("Dark", "    ") + "\n  }\n}",
    css,
    count=1,
    flags=re.S,
)
assert light_count == dark_count == 1, "Expected one light and one dark palette"
stylesheet.write_text(css, encoding="utf-8")
