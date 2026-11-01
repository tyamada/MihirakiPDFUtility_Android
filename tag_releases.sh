#!/bin/bash

DRY_RUN=false
if [ "$1" == "--dry-run" ]; then
    DRY_RUN=true
    echo "=== Running in DRY-RUN mode (no tags will be created) ==="
fi

last_commit=""
last_v_name=""

git log --reverse --pretty=format:"%H %s" | while read -r commit msg; do
    # app/build.gradle.kts から versionName を取得
    v_name=$(git show "$commit:app/build.gradle.kts" 2>/dev/null | grep -E "versionName\s*=" | sed -E "s/.*\"([^\"]+)\".*/\1/")

    if [ -n "$v_name" ]; then
        if [ "$v_name" != "$last_v_name" ]; then
            last_v_name="$v_name"
            target_commit=""
        fi

        # versionName 変更後の最初の App Bundle ビルドコミットを検出
        if [ -z "$target_commit" ] && (echo "$msg" | grep -qiE "bundle|app bundle|aab|release.*bundle"); then
            target_commit="$commit"
            tag_name="v$v_name"

            # 既にタグが存在する場合はスキップ（実運用時）
            if [ "$DRY_RUN" = false ] && git rev-parse "$tag_name" >/dev/null 2>&1; then
                echo "Tag $tag_name already exists. Skipping."
                last_commit="$target_commit"
                continue
            fi

            # メッセージ作成: 前回のタグ位置から今回のターゲットコミットまでのコミットメッセージ
            if [ -n "$last_commit" ]; then
                tag_msg=$(git log "${last_commit}..${target_commit}" --pretty=format:"- %s")
            else
                tag_msg=$(git log "${target_commit}" --pretty=format:"- %s")
            fi

            if [ "$DRY_RUN" = true ]; then
                echo "[DRY-RUN] Would create tag: $tag_name at commit $target_commit"
                echo "  Message preview:"
                echo "$tag_msg" | sed 's/^/    /'
                echo "--------------------------------------------------"
            else
                echo "Creating tag $tag_name at commit $target_commit..."
                git tag -a "$tag_name" "$target_commit" -m "Release $tag_name

$tag_msg"
            fi

            last_commit="$target_commit"
        fi
    fi
done

if [ "$DRY_RUN" = true ]; then
    echo "=== Dry-run completed. Run without --dry-run to apply tags. ==="
else
    echo "Tagging complete! Run 'git push origin --tags' to push tags to remote."
fi
