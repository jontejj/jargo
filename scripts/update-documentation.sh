#!/bin/bash
set -e # Exit with nonzero exit code if anything fails

SOURCE_BRANCH="master"
TARGET_BRANCH="gh-pages"

echo "$TRAVIS_REPO_SLUG $TRAVIS_JDK_VERSION $TRAVIS_PULL_REQUEST $TRAVIS_BRANCH"

if [ "$TRAVIS_REPO_SLUG" == "jontejj/jargo" ] && [ "$TRAVIS_JDK_VERSION" == "oraclejdk8" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "$SOURCE_BRANCH" ]; then

    echo "Saving some useful information"
    REPO=`git config remote.origin.url`
    SSH_REPO=${REPO/https:\/\/github.com\//git@github.com:}
    SHA=`git rev-parse --verify HEAD`

    echo "Generating documentation"
    mvn --quiet "javadoc:javadoc"

    IFS=$'\n'
    modules=($(mvn help:evaluate -Dexpression=project.modules | grep -v "^\[" | grep -v "<\/*strings>" | sed 's/<\/*string>//g' | sed 's/[[:space:]]//'))

    echo "Cloning the code for this repo"
    git clone $REPO $TARGET_BRANCH
    cd $TARGET_BRANCH
    # Create a new empty branch if $TARGET_BRANCH doesn't exist yet (should only happen on first deploy)
    git checkout $TARGET_BRANCH || git checkout --orphan $TARGET_BRANCH
    git config user.name "Travis CI"
    git config user.email "$COMMIT_AUTHOR_EMAIL"

    echo "Cleaning out old javadoc in repo"
    rm -rf javadoc/**/* || exit 0

    for module in "${modules[@]}"
    do
        mkdir -p javadoc/$module
        cp -R ../$module/target/site/apidocs/* javadoc/$module/
    done

    if [ -z `git diff --exit-code` ]; then
        echo "No changes to the documentation on this push; exiting."
        exit 0
    fi

    echo "Commit the updated files"
    git add --all
    git commit -m "Deploy to GitHub Pages: ${SHA}"

    # Get the deploy key by using Travis's stored variables to decrypt deploy_key.enc
    ENCRYPTED_KEY_VAR="encrypted_${ENCRYPTION_LABEL}_key"
    ENCRYPTED_IV_VAR="encrypted_${ENCRYPTION_LABEL}_iv"
    ENCRYPTED_KEY=${!ENCRYPTED_KEY_VAR}
    ENCRYPTED_IV=${!ENCRYPTED_IV_VAR}
    openssl aes-256-cbc -K $ENCRYPTED_KEY -iv $ENCRYPTED_IV -in deploy_key.enc -out deploy_key -d
    chmod 600 deploy_key
    eval `ssh-agent -s`
    ssh-add deploy_key

    git push $SSH_REPO $TARGET_BRANCH

    echo -e "Update of documentation complete\n"
fi