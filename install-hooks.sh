#!/bin/sh
echo "Installing git hooks..."
cp hooks/pre-commit .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
echo "Done."
