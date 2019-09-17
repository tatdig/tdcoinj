/*
 * Copyright 2013 Google Inc.
 * Copyright 2018 Andreas Schildbach
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

package org.tdcoinj.params;

import org.tdcoinj.core.Block;

import java.math.BigInteger;

import static com.google.common.base.Preconditions.checkState;

/**
 * Network parameters for the regression test mode of tdcoind in which all blocks are trivially solvable.
 */
public class RegTestParams extends AbstractTdcoinNetParams {
    private static final BigInteger MAX_TARGET = new BigInteger("7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff", 16);

    public RegTestParams() {
        super();
        packetMagic = 0x12fc2b1eL; //TDCOIN
        addressHeader = 111;
        p2shHeader = 196;
        targetTimespan = TARGET_TIMESPAN;
        dumpedPrivateKeyHeader = 239;
        segwitAddressHrp = "tdrt";
        genesisBlock.setTime(1555135009L); // TDCOIN
        genesisBlock.setDifficultyTarget(0x207fffff); //TDCOIN
        genesisBlock.setNonce(0);
        spendableCoinbaseDepth = 100;
        String genesisHash = genesisBlock.getHashAsString();
        checkState(genesisHash.equals("4e5c61ed3d76eb4ba44f9715f06ab7726e0e63249a3504ad4f536c5c9326cdbd")); //TDCOIN
        dnsSeeds = null;
        addrSeeds = null;
        bip32HeaderP2PKHpub = 0x043587cf; // The 4 byte header that serializes in base58 to "tpub".
        bip32HeaderP2PKHpriv = 0x04358394; // The 4 byte header that serializes in base58 to "tprv"
        bip32HeaderP2WPKHpub = 0x045f1cf6; // The 4 byte header that serializes in base58 to "vpub".
        bip32HeaderP2WPKHpriv = 0x045f18bc; // The 4 byte header that serializes in base58 to "vprv"

        // Difficulty adjustments are disabled for regtest.
        // By setting the block interval for difficulty adjustments to Integer.MAX_VALUE we make sure difficulty never
        // changes.
        interval = Integer.MAX_VALUE;
        maxTarget = MAX_TARGET;
        subsidyDecreaseBlockCount = 150;
        port = 19444; //TDCOIN
        id = ID_REGTEST;

        majorityEnforceBlockUpgrade = MainNetParams.MAINNET_MAJORITY_ENFORCE_BLOCK_UPGRADE;
        majorityRejectBlockOutdated = MainNetParams.MAINNET_MAJORITY_REJECT_BLOCK_OUTDATED;
        majorityWindow = MainNetParams.MAINNET_MAJORITY_WINDOW;
    }

    @Override
    public boolean allowEmptyPeerChain() {
        return true;
    }

    private static Block genesis;

    @Override
    public Block getGenesisBlock() {
        synchronized (RegTestParams.class) {
            if (genesis == null) {
                genesis = super.getGenesisBlock();
                genesis.setNonce(0); //TDCOIN
                genesis.setDifficultyTarget(0x207fFFFFL); //TDCOIN
                genesis.setTime(1555135009L); //TDCOIN
                checkState(genesis.getHashAsString().toLowerCase().equals("4e5c61ed3d76eb4ba44f9715f06ab7726e0e63249a3504ad4f536c5c9326cdbd")); //TDCOIN
            }
            return genesis;
        }
    }

    private static RegTestParams instance;
    public static synchronized RegTestParams get() {
        if (instance == null) {
            instance = new RegTestParams();
        }
        return instance;
    }

    @Override
    public String getPaymentProtocolId() {
        return PAYMENT_PROTOCOL_ID_REGTEST;
    }
}
