#!/usr/bin/env python3
import os
import re
import pathlib
import datetime

ROOT = pathlib.Path(__file__).resolve().parent.parent

def get_gradle_version():
    build_gradle = ROOT / "app" / "build.gradle.kts"
    version_name = "2.0.0"
    version_code = "31"
    if build_gradle.exists():
        content = build_gradle.read_text(encoding="utf-8")
        m_name = re.search(r'versionName\s*=\s*"([^"]+)"', content)
        if m_name:
            version_name = m_name.group(1)
        m_code = re.search(r'versionCode\s*=\s*(\d+)', content)
        if m_code:
            version_code = m_code.group(1)
    return version_name, version_code

def update_file_content(path, version_name, version_code):
    if not path.exists():
        return False
    text = path.read_text(encoding="utf-8")
    original = text

    # Update version code / version name patterns if present
    text = re.sub(r'Code\s+\d+', f'Code {version_code}', text, flags=re.IGNORECASE)
    text = re.sub(r'コード\s*\d+', f'コード {version_code}', text)
    text = re.sub(r'v\d+\.\d+\.\d+', f'v{version_name}', text)

    if text != original:
        path.write_text(text, encoding="utf-8")
        print(f"Updated version/help info in {path.relative_to(ROOT)}")
        return True
    return False

def verify_help_screen_strings():
    # Verify that help strings exist in strings.xml and are synchronized
    res_dir = ROOT / "app" / "src" / "main" / "res"
    values_dir = res_dir / "values"
    values_ja_dir = res_dir / "values-ja"

    help_keys_checked = ["help_open", "help_navigate", "help_search", "help_outline", "help_slider", "help_diagnostics"]
    print(f"Verifying help screen strings ({len(help_keys_checked)} key categories)...")
    for d in [values_dir, values_ja_dir]:
        strings_file = d / "strings.xml"
        if strings_file.exists():
            content = strings_file.read_text(encoding="utf-8")
            for key in help_keys_checked:
                if f'name="{key}"' not in content:
                    print(f"WARNING: Help key '{key}' not found in {strings_file.relative_to(ROOT)}")

def main():
    version_name, version_code = get_gradle_version()
    print(f"==================================================")
    print(f"Running Help Screen, README, Docs, & Store Listing Update")
    print(f"Target Version: v{version_name} (Code {version_code})")
    print(f"==================================================")

    verify_help_screen_strings()

    updated_count = targets = 0

    # 1. README files
    for readme in [ROOT / "README.md", ROOT / "README_ja.md"]:
        targets += 1
        if update_file_content(readme, version_name, version_code):
            updated_count += 1

    # 2. docs/*.html (Help / Support / Privacy / Index)
    docs_dir = ROOT / "docs"
    if docs_dir.exists():
        for html_file in docs_dir.glob("*.html"):
            targets += 1
            if update_file_content(html_file, version_name, version_code):
                updated_count += 1

    # 3. store_listing/STORE_LISTING*.md
    store_dir = ROOT / "store_listing"
    if store_dir.exists():
        for md_file in store_dir.glob("STORE_LISTING*.md"):
            targets += 1
            if update_file_content(md_file, version_name, version_code):
                updated_count += 1

    print(f"==================================================")
    print(f"Update completed successfully.")
    print(f"Files checked: {targets}, Files updated: {updated_count}")
    print(f"==================================================")

if __name__ == "__main__":
    main()
