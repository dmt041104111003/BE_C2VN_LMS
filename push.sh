#!/bin/bash

echo ""
echo "  [1] feat     - them feature moi"
echo "  [2] fix      - sua bug"
echo "  [3] refactor - sua code khong doi logic"
echo "  [4] docs     - them/sua document"
echo "  [5] style    - sua css/ui"
echo "  [6] perf     - cai thien hieu nang"
echo "  [7] chore    - viec nho nhat khac"
echo ""

read -p "Chon type (1-7): " type

case $type in
    1) prefix="feat" ;;
    2) prefix="fix" ;;
    3) prefix="refactor" ;;
    4) prefix="docs" ;;
    5) prefix="style" ;;
    6) prefix="perf" ;;
    7) prefix="chore" ;;
    *) prefix="chore" ;;
esac

read -p "Mo ta ngan: " desc

git add .
git commit -m "$prefix: $desc"
git push --force

echo ""
echo "Done: $prefix: $desc"
