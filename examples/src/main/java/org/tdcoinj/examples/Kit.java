/*
 * Copyright by the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tdcoinj.examples;

import org.tdcoinj.core.*;
import org.tdcoinj.kits.WalletAppKit;
import org.tdcoinj.params.TestNet3Params;
import org.tdcoinj.script.Script;
import org.tdcoinj.wallet.Wallet;
import org.tdcoinj.wallet.listeners.KeyChainEventListener;
import org.tdcoinj.wallet.listeners.ScriptsChangeEventListener;
import org.tdcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.tdcoinj.wallet.listeners.WalletCoinsSentEventListener;

import java.io.File;
import java.util.List;

import org.tdcoinj.core.listeners.TransactionConfidenceEventListener;

/**
 * The following example shows how to use the by tdcoinj provided WalletAppKit.
 * The WalletAppKit class wraps the boilerplate (Peers, BlockChain, BlockStorage, Wallet) needed to set up a new SPV tdcoinj app.
 * 
 * In this example we also define a WalletEventListener class with implementors that are called when the wallet changes (for example sending/receiving money)
 */
public class Kit {

    public static void main(String[] args) {

        // First we configure the network we want to use.
        // The available options are:
        // - MainNetParams
        // - TestNet3Params
        // - RegTestParams
        // While developing your application you probably want to use the Regtest mode and run your local tdcoin network. Run tdcoind with the -regtest flag
        // To test you app with a real network you can use the testnet. The testnet is an alternative tdcoin network that follows the same rules as main network. Coins are worth nothing and you can get coins for example from http://faucet.xeno-genesis.com/
        // 
        // For more information have a look at: https://tdcoinj.github.io/testing and https://tdcoin.org/en/developer-examples#testing-applications
        NetworkParameters params = TestNet3Params.get();

        // Now we initialize a new WalletAppKit. The kit handles all the boilerplate for us and is the easiest way to get everything up and running.
        // Have a look at the WalletAppKit documentation and its source to understand what's happening behind the scenes: https://github.com/tdcoinj/tdcoinj/blob/master/core/src/main/java/org/tdcoinj/kits/WalletAppKit.java
        WalletAppKit kit = new WalletAppKit(params, new File("."), "walletappkit-example");

        // In case you want to connect with your local tdcoind tell the kit to connect to localhost.
        // You must do that in reg test mode.
        //kit.connectToLocalHost();

        // Now we start the kit and sync the blockchain.
        // tdcoinj is working a lot with the Google Guava libraries. The WalletAppKit extends the AbstractIdleService. Have a look at the introduction to Guava services: https://github.com/google/guava/wiki/ServiceExplained
        kit.startAsync();
        kit.awaitRunning();

        kit.wallet().addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            @Override
            public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                System.out.println("-----> coins resceived: " + tx.getTxId());
                System.out.println("received: " + tx.getValue(wallet));
            }
        });

        kit.wallet().addCoinsSentEventListener(new WalletCoinsSentEventListener() {
            @Override
            public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                System.out.println("coins sent");
            }
        });

        kit.wallet().addKeyChainEventListener(new KeyChainEventListener() {
            @Override
            public void onKeysAdded(List<ECKey> keys) {
                System.out.println("new key added");
            }
        });

        kit.wallet().addScriptsChangeEventListener(new ScriptsChangeEventListener() {
            @Override
            public void onScriptsChanged(Wallet wallet, List<Script> scripts, boolean isAddingScripts) {
                System.out.println("new script added");
            }
        });

        kit.wallet().addTransactionConfidenceEventListener(new TransactionConfidenceEventListener() {
            @Override
            public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
                System.out.println("-----> confidence changed: " + tx.getTxId());
                TransactionConfidence confidence = tx.getConfidence();
                System.out.println("new block depth: " + confidence.getDepthInBlocks());
            }
        });

        // Ready to run. The kit syncs the blockchain and our wallet event listener gets notified when something happens.
        // To test everything we create and print a fresh receiving address. Send some coins to that address and see if everything works.
        System.out.println("send money to: " + kit.wallet().freshReceiveAddress().toString());

        // Make sure to properly shut down all the running services when you manually want to stop the kit. The WalletAppKit registers a runtime ShutdownHook so we actually do not need to worry about that when our application is stopping.
        //System.out.println("shutting down again");
        //kit.stopAsync();
        //kit.awaitTerminated();
    }

}
