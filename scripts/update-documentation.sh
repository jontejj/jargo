if [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
  echo -e "Updating documentation at http://softhouse.github.io/jargo/\n"
  #Generate documentation
  mvn --quiet "javadoc:javadoc"
  #TODO(jontejj): don't specify these manually
  mkdir -p $HOME/javadoc/jargo
  mkdir -p $HOME/javadoc/common-test
  cp -R jargo/target/site/apidocs/* $HOME/javadoc/jargo/
  cp -R common-test/target/site/apidocs/* $HOME/javadoc/common-test/
  #Setup git
  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "Travis"
  #clone gh-pages branch using encrypted gh-token
  git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/${TRAVIS_REPO_SLUG}.git gh-pages > /dev/null
  mkdir -p gh-pages/javadoc
  cd gh-pages/javadoc
  #Update files
  rm -rf jargo/*
  rm -rf common-test/*
  cp -Rf $HOME/javadoc/* .
  #Publish
  git add --all .
  git commit -m "Automatic javadoc update due to $TRAVIS_COMMIT"
  git push -fq origin gh-pages > /dev/null
  echo -e "Update of documentation complete\n"
fi
