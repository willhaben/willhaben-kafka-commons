
mvn clean deploy -PtravisRelease --settings deployment/settings.xml -Dgpg.executable=gpg2 -Dgpg.keyname=38E5AD5EB69629CE -Dgpg.publicKeyring=$TRAVIS_BUILD_DIR/pubring.gpg -Dgpg.secretKeyring=$TRAVIS_BUILD_DIR/secring.gpg
