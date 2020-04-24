#!/bin/bash

# decrypt the file containing the gpg keys
openssl aes-256-cbc -K $encrypted_171b1c559d7b_key -iv $encrypted_171b1c559d7b_iv -in deployment/signingkey.asc.enc -out deployment/signingkey.asc -d

# Create a keyring
gpg2 --keyring=$TRAVIS_BUILD_DIR/pubring.gpg --no-default-keyring --import deployment/signingkey.asc

# Load the gpg keys into the keyring
gpg2 --allow-secret-key-import --keyring=$TRAVIS_BUILD_DIR/secring.gpg --no-default-keyring --batch --import deployment/signingkey.asc
