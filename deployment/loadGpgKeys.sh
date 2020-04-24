#!/bin/bash

# Export variable to make GPG work
GPG_TTY=$(tty)
export GPG_TTY

# Allow loopback mode
echo "allow-loopback-pinentry" >> ~/.gnupg/gpg-agent.conf

# decrypt the file containing the gpg keys
openssl aes-256-cbc -K $encrypted_171b1c559d7b_key -iv $encrypted_171b1c559d7b_iv -in deployment/signingkey.asc.enc -out deployment/signingkey.asc -d

# Load the gpg key into the keyring
gpg2 --batch --no-default-keyring --keyring=$TRAVIS_BUILD_DIR/pubring.gpg --import deployment/signingkey.asc

# Load the gpg secret key into the keyring
gpg2 --batch --no-default-keyring --allow-secret-key-import --keyring=$TRAVIS_BUILD_DIR/secring.gpg --import deployment/signingkey.asc
