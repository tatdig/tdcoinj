/*
 * Copyright 2012, 2014 the original author or authors.
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

package org.tdcoinj.uri;

import org.tdcoinj.core.Address;
import org.tdcoinj.core.LegacyAddress;
import org.tdcoinj.params.MainNetParams;
import org.tdcoinj.params.TestNet3Params;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.tdcoinj.core.Coin.*;
import org.tdcoinj.core.NetworkParameters;
import org.tdcoinj.core.SegwitAddress;

import static org.junit.Assert.*;

import java.util.Locale;

public class TdcoinURITest {
    private TdcoinURI testObject = null;

    private static final NetworkParameters MAINNET = MainNetParams.get();
    private static final NetworkParameters TESTNET = TestNet3Params.get();
    private static final String MAINNET_GOOD_ADDRESS = "TEkkgToFtnajaWPBW42EDmbBi7MyEr4ybH"; //TDCOIN
    private static final String MAINNET_GOOD_SEGWIT_ADDRESS = "tc1q76n3w20ex665lfgt42m2ck4nzc5xyws45gdx96"; //TDCOIN
    private static final String TDCOIN_SCHEME = MAINNET.getUriScheme();

    @Test
    public void testConvertToTdcoinURI() throws Exception {
        Address goodAddress = LegacyAddress.fromBase58(MAINNET, MAINNET_GOOD_ADDRESS);
        
        // simple example
        assertEquals("tdcoin:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello&message=AMessage", TdcoinURI.convertToTdcoinURI(goodAddress, parseCoin("12.34"), "Hello", "AMessage"));
        
        // example with spaces, ampersand and plus
        assertEquals("tdcoin:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello%20World&message=Mess%20%26%20age%20%2B%20hope", TdcoinURI.convertToTdcoinURI(goodAddress, parseCoin("12.34"), "Hello World", "Mess & age + hope"));

        // no amount, label present, message present
        assertEquals("tdcoin:" + MAINNET_GOOD_ADDRESS + "?label=Hello&message=glory", TdcoinURI.convertToTdcoinURI(goodAddress, null, "Hello", "glory"));
        
        // amount present, no label, message present
        assertEquals("tdcoin:" + MAINNET_GOOD_ADDRESS + "?amount=0.1&message=glory", TdcoinURI.convertToTdcoinURI(goodAddress, parseCoin("0.1"), null, "glory"));
        assertEquals("tdcoin:" + MAINNET_GOOD_ADDRESS + "?amount=0.1&message=glory", TdcoinURI.convertToTdcoinURI(goodAddress, parseCoin("0.1"), "", "glory"));

        // amount present, label present, no message
        assertEquals("tdcoin:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello", TdcoinURI.convertToTdcoinURI(goodAddress, parseCoin("12.34"), "Hello", null));
        assertEquals("tdcoin:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello", TdcoinURI.convertToTdcoinURI(goodAddress, parseCoin("12.34"), "Hello", ""));
              
        // amount present, no label, no message
        assertEquals("tdcoin:" + MAINNET_GOOD_ADDRESS + "?amount=1000", TdcoinURI.convertToTdcoinURI(goodAddress, parseCoin("1000"), null, null));
        assertEquals("tdcoin:" + MAINNET_GOOD_ADDRESS + "?amount=1000", TdcoinURI.convertToTdcoinURI(goodAddress, parseCoin("1000"), "", ""));
        
        // no amount, label present, no message
        assertEquals("tdcoin:" + MAINNET_GOOD_ADDRESS + "?label=Hello", TdcoinURI.convertToTdcoinURI(goodAddress, null, "Hello", null));
        
        // no amount, no label, message present
        assertEquals("tdcoin:" + MAINNET_GOOD_ADDRESS + "?message=Agatha", TdcoinURI.convertToTdcoinURI(goodAddress, null, null, "Agatha"));
        assertEquals("tdcoin:" + MAINNET_GOOD_ADDRESS + "?message=Agatha", TdcoinURI.convertToTdcoinURI(goodAddress, null, "", "Agatha"));
      
        // no amount, no label, no message
        assertEquals("tdcoin:" + MAINNET_GOOD_ADDRESS, TdcoinURI.convertToTdcoinURI(goodAddress, null, null, null));
        assertEquals("tdcoin:" + MAINNET_GOOD_ADDRESS, TdcoinURI.convertToTdcoinURI(goodAddress, null, "", ""));

        // different scheme
        final NetworkParameters alternativeParameters = new MainNetParams() {
            @Override
            public String getUriScheme() {
                return "test";
            }
        };

        assertEquals("test:" + MAINNET_GOOD_ADDRESS + "?amount=12.34&label=Hello&message=AMessage",
             TdcoinURI.convertToTdcoinURI(LegacyAddress.fromBase58(alternativeParameters, MAINNET_GOOD_ADDRESS), parseCoin("12.34"), "Hello", "AMessage"));
    }

    @Test
    public void testConvertToTdcoinURI_segwit() throws Exception {
        assertEquals("tdcoin:" + MAINNET_GOOD_SEGWIT_ADDRESS + "?message=segwit%20rules", TdcoinURI.convertToTdcoinURI(
                SegwitAddress.fromBech32(MAINNET, MAINNET_GOOD_SEGWIT_ADDRESS), null, null, "segwit rules"));
    }

    @Test
    public void testGood_legacy() throws TdcoinURIParseException {
        testObject = new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS);
        assertEquals(MAINNET_GOOD_ADDRESS, testObject.getAddress().toString());
        assertNull("Unexpected amount", testObject.getAmount());
        assertNull("Unexpected label", testObject.getLabel());
        assertEquals("Unexpected label", 20, testObject.getAddress().getHash().length);
    }

    @Test
    public void testGood_uppercaseScheme() throws TdcoinURIParseException {
        testObject = new TdcoinURI(MAINNET, TDCOIN_SCHEME.toUpperCase(Locale.US) + ":" + MAINNET_GOOD_ADDRESS);
        assertEquals(MAINNET_GOOD_ADDRESS, testObject.getAddress().toString());
        assertNull("Unexpected amount", testObject.getAmount());
        assertNull("Unexpected label", testObject.getLabel());
        assertEquals("Unexpected label", 20, testObject.getAddress().getHash().length);
    }

    @Test
    public void testGood_segwit() throws TdcoinURIParseException {
        testObject = new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":" + MAINNET_GOOD_SEGWIT_ADDRESS);
        assertEquals(MAINNET_GOOD_SEGWIT_ADDRESS, testObject.getAddress().toString());
        assertNull("Unexpected amount", testObject.getAmount());
        assertNull("Unexpected label", testObject.getLabel());
    }

    /**
     * Test a broken URI (bad scheme)
     */
    @Test
    public void testBad_Scheme() {
        try {
            testObject = new TdcoinURI(MAINNET, "blimpcoin:" + MAINNET_GOOD_ADDRESS);
            fail("Expecting TdcoinURIParseException");
        } catch (TdcoinURIParseException e) {
        }
    }

    /**
     * Test a broken URI (bad syntax)
     */
    @Test
    public void testBad_BadSyntax() {
        // Various illegal characters
        try {
            testObject = new TdcoinURI(MAINNET, TDCOIN_SCHEME + "|" + MAINNET_GOOD_ADDRESS);
            fail("Expecting TdcoinURIParseException");
        } catch (TdcoinURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }

        try {
            testObject = new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS + "\\");
            fail("Expecting TdcoinURIParseException");
        } catch (TdcoinURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }

        // Separator without field
        try {
            testObject = new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":");
            fail("Expecting TdcoinURIParseException");
        } catch (TdcoinURIParseException e) {
            assertTrue(e.getMessage().contains("Bad URI syntax"));
        }
    }

    /**
     * Test a broken URI (missing address)
     */
    @Test
    public void testBad_Address() {
        try {
            testObject = new TdcoinURI(MAINNET, TDCOIN_SCHEME);
            fail("Expecting TdcoinURIParseException");
        } catch (TdcoinURIParseException e) {
        }
    }

    /**
     * Test a broken URI (bad address type)
     */
    @Test
    public void testBad_IncorrectAddressType() {
        try {
            testObject = new TdcoinURI(TESTNET, TDCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS);
            fail("Expecting TdcoinURIParseException");
        } catch (TdcoinURIParseException e) {
            assertTrue(e.getMessage().contains("Bad address"));
        }
    }

    /**
     * Handles a simple amount
     * 
     * @throws TdcoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Amount() throws TdcoinURIParseException {
        // Test the decimal parsing
        testObject = new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=6543210.12345678");
        assertEquals("654321012345678", testObject.getAmount().toString());

        // Test the decimal parsing
        testObject = new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=.12345678");
        assertEquals("12345678", testObject.getAmount().toString());

        // Test the integer parsing
        testObject = new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=6543210");
        assertEquals("654321000000000", testObject.getAmount().toString());
    }

    /**
     * Handles a simple label
     * 
     * @throws TdcoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Label() throws TdcoinURIParseException {
        testObject = new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?label=Hello%20World");
        assertEquals("Hello World", testObject.getLabel());
    }

    /**
     * Handles a simple label with an embedded ampersand and plus
     * 
     * @throws TdcoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_LabelWithAmpersandAndPlus() throws TdcoinURIParseException {
        String testString = "Hello Earth & Mars + Venus";
        String encodedLabel = TdcoinURI.encodeURLString(testString);
        testObject = new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS + "?label="
                + encodedLabel);
        assertEquals(testString, testObject.getLabel());
    }

    /**
     * Handles a Russian label (Unicode test)
     * 
     * @throws TdcoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_LabelWithRussian() throws TdcoinURIParseException {
        // Moscow in Russian in Cyrillic
        String moscowString = "\u041c\u043e\u0441\u043a\u0432\u0430";
        String encodedLabel = TdcoinURI.encodeURLString(moscowString); 
        testObject = new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS + "?label="
                + encodedLabel);
        assertEquals(moscowString, testObject.getLabel());
    }

    /**
     * Handles a simple message
     * 
     * @throws TdcoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Message() throws TdcoinURIParseException {
        testObject = new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?message=Hello%20World");
        assertEquals("Hello World", testObject.getMessage());
    }

    /**
     * Handles various well-formed combinations
     * 
     * @throws TdcoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testGood_Combinations() throws TdcoinURIParseException {
        testObject = new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=6543210&label=Hello%20World&message=Be%20well");
        assertEquals(
                "TdcoinURI['amount'='654321000000000','label'='Hello World','message'='Be well','address'='TEkkgToFtnajaWPBW42EDmbBi7MyEr4ybH']",
                testObject.toString());
    }

    /**
     * Handles a badly formatted amount field
     * 
     * @throws TdcoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_Amount() throws TdcoinURIParseException {
        // Missing
        try {
            testObject = new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?amount=");
            fail("Expecting TdcoinURIParseException");
        } catch (TdcoinURIParseException e) {
            assertTrue(e.getMessage().contains("amount"));
        }

        // Non-decimal (BIP 21)
        try {
            testObject = new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?amount=12X4");
            fail("Expecting TdcoinURIParseException");
        } catch (TdcoinURIParseException e) {
            assertTrue(e.getMessage().contains("amount"));
        }
    }

    @Test
    public void testEmpty_Label() throws TdcoinURIParseException {
        assertNull(new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?label=").getLabel());
    }

    @Test
    public void testEmpty_Message() throws TdcoinURIParseException {
        assertNull(new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?message=").getMessage());
    }

    /**
     * Handles duplicated fields (sneaky address overwrite attack)
     * 
     * @throws TdcoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testBad_Duplicated() throws TdcoinURIParseException {
        try {
            testObject = new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?address=aardvark");
            fail("Expecting TdcoinURIParseException");
        } catch (TdcoinURIParseException e) {
            assertTrue(e.getMessage().contains("address"));
        }
    }

    @Test
    public void testGood_ManyEquals() throws TdcoinURIParseException {
        assertEquals("aardvark=zebra", new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":"
                + MAINNET_GOOD_ADDRESS + "?label=aardvark=zebra").getLabel());
    }
    
    /**
     * Handles unknown fields (required and not required)
     * 
     * @throws TdcoinURIParseException
     *             If something goes wrong
     */
    @Test
    public void testUnknown() throws TdcoinURIParseException {
        // Unknown not required field
        testObject = new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?aardvark=true");
        assertEquals("TdcoinURI['aardvark'='true','address'='TEkkgToFtnajaWPBW42EDmbBi7MyEr4ybH']", testObject.toString());

        assertEquals("true", testObject.getParameterByName("aardvark"));

        // Unknown not required field (isolated)
        try {
            testObject = new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?aardvark");
            fail("Expecting TdcoinURIParseException");
        } catch (TdcoinURIParseException e) {
            assertTrue(e.getMessage().contains("no separator"));
        }

        // Unknown and required field
        try {
            testObject = new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                    + "?req-aardvark=true");
            fail("Expecting TdcoinURIParseException");
        } catch (TdcoinURIParseException e) {
            assertTrue(e.getMessage().contains("req-aardvark"));
        }
    }

    @Test
    public void brokenURIs() throws TdcoinURIParseException {
        // Check we can parse the incorrectly formatted URIs produced by blockchain.info and its iPhone app.
        String str = "tdcoin://TEkkgToFtnajaWPBW42EDmbBi7MyEr4ybH?amount=0.01000000";
        TdcoinURI uri = new TdcoinURI(str);
        assertEquals("TEkkgToFtnajaWPBW42EDmbBi7MyEr4ybH", uri.getAddress().toString());
        assertEquals(CENT, uri.getAmount());
    }

    @Test(expected = TdcoinURIParseException.class)
    public void testBad_AmountTooPrecise() throws TdcoinURIParseException {
        new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=0.123456789");
    }

    @Test(expected = TdcoinURIParseException.class)
    public void testBad_NegativeAmount() throws TdcoinURIParseException {
        new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=-1");
    }

    @Test(expected = TdcoinURIParseException.class)
    public void testBad_TooLargeAmount() throws TdcoinURIParseException {
        new TdcoinURI(MAINNET, TDCOIN_SCHEME + ":" + MAINNET_GOOD_ADDRESS
                + "?amount=10000000000");
    }

    @Test
    public void testPaymentProtocolReq() throws Exception {
        // Non-backwards compatible form ...
        TdcoinURI uri = new TdcoinURI(TESTNET, "tdcoin:?r=https%3A%2F%2Ftdcoincore.org%2F%7Egavin%2Ff.php%3Fh%3Db0f02e7cea67f168e25ec9b9f9d584f9");
        assertEquals("https://tdcoincore.org/~gavin/f.php?h=b0f02e7cea67f168e25ec9b9f9d584f9", uri.getPaymentRequestUrl());
        assertEquals(ImmutableList.of("https://tdcoincore.org/~gavin/f.php?h=b0f02e7cea67f168e25ec9b9f9d584f9"),
                uri.getPaymentRequestUrls());
        assertNull(uri.getAddress());
    }

    @Test
    public void testMultiplePaymentProtocolReq() throws Exception {
        TdcoinURI uri = new TdcoinURI(MAINNET,
                "tdcoin:?r=https%3A%2F%2Ftdcoincore.org%2F%7Egavin&r1=bt:112233445566");
        assertEquals(ImmutableList.of("bt:112233445566", "https://tdcoincore.org/~gavin"), uri.getPaymentRequestUrls());
        assertEquals("https://tdcoincore.org/~gavin", uri.getPaymentRequestUrl());
    }

    @Test
    public void testNoPaymentProtocolReq() throws Exception {
        TdcoinURI uri = new TdcoinURI(MAINNET, "tdcoin:" + MAINNET_GOOD_ADDRESS);
        assertNull(uri.getPaymentRequestUrl());
        assertEquals(ImmutableList.of(), uri.getPaymentRequestUrls());
        assertNotNull(uri.getAddress());
    }

    @Test
    public void testUnescapedPaymentProtocolReq() throws Exception {
        TdcoinURI uri = new TdcoinURI(TESTNET,
                "tdcoin:?r=https://merchant.com/pay.php?h%3D2a8628fc2fbe");
        assertEquals("https://merchant.com/pay.php?h=2a8628fc2fbe", uri.getPaymentRequestUrl());
        assertEquals(ImmutableList.of("https://merchant.com/pay.php?h=2a8628fc2fbe"), uri.getPaymentRequestUrls());
        assertNull(uri.getAddress());
    }
}
