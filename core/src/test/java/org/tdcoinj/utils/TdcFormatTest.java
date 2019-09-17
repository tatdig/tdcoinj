/*
 * Copyright 2014 Adam Mackler
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

package org.tdcoinj.utils;

import org.tdcoinj.core.Coin;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigDecimal;
import java.text.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.tdcoinj.core.Coin.*;
import static org.tdcoinj.core.NetworkParameters.MAX_MONEY;
import static org.tdcoinj.utils.TdcAutoFormat.Style.CODE;
import static org.tdcoinj.utils.TdcAutoFormat.Style.SYMBOL;
import static org.tdcoinj.utils.TdcFixedFormat.REPEATING_DOUBLETS;
import static org.tdcoinj.utils.TdcFixedFormat.REPEATING_TRIPLETS;
import static java.text.NumberFormat.Field.DECIMAL_SEPARATOR;
import static java.util.Locale.*;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class TdcFormatTest {

    @Parameters
    public static Set<Locale[]> data() {
        Set<Locale[]> localeSet = new HashSet<>();
        for (Locale locale : Locale.getAvailableLocales()) {
            localeSet.add(new Locale[]{locale});
        }
        return localeSet;
    }

    public TdcFormatTest(Locale defaultLocale) {
        Locale.setDefault(defaultLocale);
    }
 
    @Test
    public void prefixTest() { // prefix b/c symbol is prefixed
        TdcFormat usFormat = TdcFormat.getSymbolInstance(Locale.US);
        assertEquals("฿1.00", usFormat.format(COIN));
        assertEquals("฿1.01", usFormat.format(101000000));
        assertEquals("₥฿0.01", usFormat.format(1000));
        assertEquals("₥฿1,011.00", usFormat.format(101100000));
        assertEquals("₥฿1,000.01", usFormat.format(100001000));
        assertEquals("µ฿1,000,001.00", usFormat.format(100000100));
        assertEquals("µ฿1,000,000.10", usFormat.format(100000010));
        assertEquals("µ฿1,000,000.01", usFormat.format(100000001));
        assertEquals("µ฿1.00", usFormat.format(100));
        assertEquals("µ฿0.10", usFormat.format(10));
        assertEquals("µ฿0.01", usFormat.format(1));
    }

    @Ignore("non-determinism between OpenJDK versions")
    @Test
    public void suffixTest() {
        TdcFormat deFormat = TdcFormat.getSymbolInstance(Locale.GERMANY);
        // int
        assertEquals("1,00 ฿", deFormat.format(100000000));
        assertEquals("1,01 ฿", deFormat.format(101000000));
        assertEquals("1.011,00 ₥฿", deFormat.format(101100000));
        assertEquals("1.000,01 ₥฿", deFormat.format(100001000));
        assertEquals("1.000.001,00 µ฿", deFormat.format(100000100));
        assertEquals("1.000.000,10 µ฿", deFormat.format(100000010));
        assertEquals("1.000.000,01 µ฿", deFormat.format(100000001));
    }

    @Test
    public void defaultLocaleTest() {
        assertEquals(
             "Default Locale is " + Locale.getDefault().toString(),
             TdcFormat.getInstance().pattern(), TdcFormat.getInstance(Locale.getDefault()).pattern()
        );
        assertEquals(
            "Default Locale is " + Locale.getDefault().toString(),
            TdcFormat.getCodeInstance().pattern(),
            TdcFormat.getCodeInstance(Locale.getDefault()).pattern()
       );
    }

    @Test
    public void symbolCollisionTest() {
        Locale[] locales = TdcFormat.getAvailableLocales();
        for (int i = 0; i < locales.length; ++i) {
            String cs = ((DecimalFormat)NumberFormat.getCurrencyInstance(locales[i])).
                        getDecimalFormatSymbols().getCurrencySymbol();
            if (cs.contains("฿")) {
                TdcFormat bf = TdcFormat.getSymbolInstance(locales[i]);
                String coin = bf.format(COIN);
                assertTrue(coin.contains("Ƀ"));
                assertFalse(coin.contains("฿"));
                String milli = bf.format(valueOf(10000));
                assertTrue(milli.contains("₥Ƀ"));
                assertFalse(milli.contains("฿"));
                String micro = bf.format(valueOf(100));
                assertTrue(micro.contains("µɃ"));
                assertFalse(micro.contains("฿"));
                TdcFormat ff = TdcFormat.builder().scale(0).locale(locales[i]).pattern("¤#.#").build();
                assertEquals("Ƀ", ((TdcFixedFormat)ff).symbol());
                assertEquals("Ƀ", ff.coinSymbol());
                coin = ff.format(COIN);
                assertTrue(coin.contains("Ƀ"));
                assertFalse(coin.contains("฿"));
                TdcFormat mlff = TdcFormat.builder().scale(3).locale(locales[i]).pattern("¤#.#").build();
                assertEquals("₥Ƀ", ((TdcFixedFormat)mlff).symbol());
                assertEquals("Ƀ", mlff.coinSymbol());
                milli = mlff.format(valueOf(10000));
                assertTrue(milli.contains("₥Ƀ"));
                assertFalse(milli.contains("฿"));
                TdcFormat mcff = TdcFormat.builder().scale(6).locale(locales[i]).pattern("¤#.#").build();
                assertEquals("µɃ", ((TdcFixedFormat)mcff).symbol());
                assertEquals("Ƀ", mcff.coinSymbol());
                micro = mcff.format(valueOf(100));
                assertTrue(micro.contains("µɃ"));
                assertFalse(micro.contains("฿"));
            }
            if (cs.contains("Ƀ")) {  // NB: We don't know of any such existing locale, but check anyway.
                TdcFormat bf = TdcFormat.getInstance(locales[i]);
                String coin = bf.format(COIN);
                assertTrue(coin.contains("฿"));
                assertFalse(coin.contains("Ƀ"));
                String milli = bf.format(valueOf(10000));
                assertTrue(milli.contains("₥฿"));
                assertFalse(milli.contains("Ƀ"));
                String micro = bf.format(valueOf(100));
                assertTrue(micro.contains("µ฿"));
                assertFalse(micro.contains("Ƀ"));
            }
        }
    }

    @Ignore("non-determinism between OpenJDK versions")
    @Test
    public void argumentTypeTest() {
        TdcFormat usFormat = TdcFormat.getSymbolInstance(Locale.US);
        // longs are tested above
        // Coin
        assertEquals("µ฿1,000,000.01", usFormat.format(COIN.add(valueOf(1))));
        // Integer
        assertEquals("µ฿21,474,836.47" ,usFormat.format(Integer.MAX_VALUE));
        assertEquals("(µ฿21,474,836.48)" ,usFormat.format(Integer.MIN_VALUE));
        // Long
        assertEquals("µ฿92,233,720,368,547,758.07" ,usFormat.format(Long.MAX_VALUE));
        assertEquals("(µ฿92,233,720,368,547,758.08)" ,usFormat.format(Long.MIN_VALUE));
        // BigInteger
        assertEquals("µ฿0.10" ,usFormat.format(java.math.BigInteger.TEN));
        assertEquals("฿0.00" ,usFormat.format(java.math.BigInteger.ZERO));
        // BigDecimal
        assertEquals("฿1.00" ,usFormat.format(java.math.BigDecimal.ONE));
        assertEquals("฿0.00" ,usFormat.format(java.math.BigDecimal.ZERO));
        // use of Double not encouraged but no way to stop user from converting one to BigDecimal
        assertEquals(
            "฿179,769,313,486,231,570,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000.00",
            usFormat.format(java.math.BigDecimal.valueOf(Double.MAX_VALUE)));
        assertEquals("฿0.00", usFormat.format(java.math.BigDecimal.valueOf(Double.MIN_VALUE)));
        assertEquals(
            "฿340,282,346,638,528,860,000,000,000,000,000,000,000.00",
            usFormat.format(java.math.BigDecimal.valueOf(Float.MAX_VALUE)));
        // Bad type
        try {
            usFormat.format("1");
            fail("should not have tried to format a String");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void columnAlignmentTest() {
        TdcFormat germany = TdcFormat.getCoinInstance(2,TdcFixedFormat.REPEATING_PLACES);
        char separator = germany.symbols().getDecimalSeparator();
        Coin[] rows = {MAX_MONEY, MAX_MONEY.subtract(SATOSHI), Coin.parseCoin("1234"),
                       COIN, COIN.add(SATOSHI), COIN.subtract(SATOSHI),
                        COIN.divide(1000).add(SATOSHI), COIN.divide(1000), COIN.divide(1000).subtract(SATOSHI),
                       valueOf(100), valueOf(1000), valueOf(10000),
                       SATOSHI};
        FieldPosition fp = new FieldPosition(DECIMAL_SEPARATOR);
        String[] output = new String[rows.length];
        int[] indexes = new int[rows.length];
        int maxIndex = 0;
        for (int i = 0; i < rows.length; i++) {
            output[i] = germany.format(rows[i], new StringBuffer(), fp).toString();
            indexes[i] = fp.getBeginIndex();
            if (indexes[i] > maxIndex) maxIndex = indexes[i];
        }
        for (int i = 0; i < output.length; i++) {
            // uncomment to watch printout
            // System.out.println(repeat(" ", (maxIndex - indexes[i])) + output[i]);
            assertEquals(output[i].indexOf(separator), indexes[i]);
        }
    }

    @Test
    public void repeatingPlaceTest() {
        TdcFormat mega = TdcFormat.getInstance(-6, US);
        Coin value = MAX_MONEY.subtract(SATOSHI);
        assertEquals("1,999.99999999999999", mega.format(value, 0, TdcFixedFormat.REPEATING_PLACES));
        assertEquals("1,999.99999999999999", mega.format(value, 0, TdcFixedFormat.REPEATING_PLACES));
        assertEquals("1,999.99999999999999", mega.format(value, 1, TdcFixedFormat.REPEATING_PLACES));
        assertEquals("1,999.99999999999999", mega.format(value, 2, TdcFixedFormat.REPEATING_PLACES));
        assertEquals("1,999.99999999999999", mega.format(value, 3, TdcFixedFormat.REPEATING_PLACES));
        assertEquals("1,999.99999999999999", mega.format(value, 0, TdcFixedFormat.REPEATING_DOUBLETS));
        assertEquals("1,999.99999999999999", mega.format(value, 1, TdcFixedFormat.REPEATING_DOUBLETS));
        assertEquals("1,999.99999999999999", mega.format(value, 2, TdcFixedFormat.REPEATING_DOUBLETS));
        assertEquals("1,999.99999999999999", mega.format(value, 3, TdcFixedFormat.REPEATING_DOUBLETS));
        assertEquals("1,999.99999999999999", mega.format(value, 0, TdcFixedFormat.REPEATING_TRIPLETS));
        assertEquals("1,999.99999999999999", mega.format(value, 1, TdcFixedFormat.REPEATING_TRIPLETS));
        assertEquals("1,999.99999999999999", mega.format(value, 2, TdcFixedFormat.REPEATING_TRIPLETS));
        assertEquals("1,999.99999999999999", mega.format(value, 3, TdcFixedFormat.REPEATING_TRIPLETS));
        assertEquals("1.00000005", TdcFormat.getCoinInstance(US).
                                   format(COIN.add(Coin.valueOf(5)), 0, TdcFixedFormat.REPEATING_PLACES));
    }

    @Test
    public void characterIteratorTest() {
        TdcFormat usFormat = TdcFormat.getInstance(Locale.US);
        AttributedCharacterIterator i = usFormat.formatToCharacterIterator(parseCoin("1234.5"));
        java.util.Set<Attribute> a = i.getAllAttributeKeys();
        assertTrue("Missing currency attribute", a.contains(NumberFormat.Field.CURRENCY));
        assertTrue("Missing integer attribute", a.contains(NumberFormat.Field.INTEGER));
        assertTrue("Missing fraction attribute", a.contains(NumberFormat.Field.FRACTION));
        assertTrue("Missing decimal separator attribute", a.contains(NumberFormat.Field.DECIMAL_SEPARATOR));
        assertTrue("Missing grouping separator attribute", a.contains(NumberFormat.Field.GROUPING_SEPARATOR));
        assertTrue("Missing currency attribute", a.contains(NumberFormat.Field.CURRENCY));

        char c;
        i = TdcFormat.getCodeInstance(Locale.US).formatToCharacterIterator(new BigDecimal("0.19246362747414458"));
        // formatted as "µTDC 192,463.63"
        assertEquals(0, i.getBeginIndex());
        assertEquals(15, i.getEndIndex());
        int n = 0;
        for(c = i.first(); i.getAttribute(NumberFormat.Field.CURRENCY) != null; c = i.next()) {
            n++;
        }
        assertEquals(4, n);
        n = 0;
        for(i.next(); i.getAttribute(NumberFormat.Field.INTEGER) != null && i.getAttribute(NumberFormat.Field.GROUPING_SEPARATOR) != NumberFormat.Field.GROUPING_SEPARATOR; c = i.next()) {
            n++;
        }
        assertEquals(3, n);
        assertEquals(NumberFormat.Field.INTEGER, i.getAttribute(NumberFormat.Field.INTEGER));
        n = 0;
        for(c = i.next(); i.getAttribute(NumberFormat.Field.INTEGER) != null; c = i.next()) {
            n++;
        }
        assertEquals(3, n);
        assertEquals(NumberFormat.Field.DECIMAL_SEPARATOR, i.getAttribute(NumberFormat.Field.DECIMAL_SEPARATOR));
        n = 0;
        for(c = i.next(); c != CharacterIterator.DONE; c = i.next()) {
            n++;
            assertNotNull(i.getAttribute(NumberFormat.Field.FRACTION));
        }
        assertEquals(2,n);

        // immutability check
        TdcFormat fa = TdcFormat.getSymbolInstance(US);
        TdcFormat fb = TdcFormat.getSymbolInstance(US);
        assertEquals(fa, fb);
        assertEquals(fa.hashCode(), fb.hashCode());
        fa.formatToCharacterIterator(COIN.multiply(1000000));
        assertEquals(fa, fb);
        assertEquals(fa.hashCode(), fb.hashCode());
        fb.formatToCharacterIterator(COIN.divide(1000000));
        assertEquals(fa, fb);
        assertEquals(fa.hashCode(), fb.hashCode());
    }

    @Ignore("non-determinism between OpenJDK versions")
    @Test
    public void parseTest() throws java.text.ParseException {
        TdcFormat us = TdcFormat.getSymbolInstance(Locale.US);
        TdcFormat usCoded = TdcFormat.getCodeInstance(Locale.US);
        // Coins
        assertEquals(valueOf(200000000), us.parseObject("TDC2"));
        assertEquals(valueOf(200000000), us.parseObject("XBT2"));
        assertEquals(valueOf(200000000), us.parseObject("฿2"));
        assertEquals(valueOf(200000000), us.parseObject("Ƀ2"));
        assertEquals(valueOf(200000000), us.parseObject("2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("TDC 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("XBT 2"));
        assertEquals(valueOf(200000000), us.parseObject("฿2.0"));
        assertEquals(valueOf(200000000), us.parseObject("Ƀ2.0"));
        assertEquals(valueOf(200000000), us.parseObject("2.0"));
        assertEquals(valueOf(200000000), us.parseObject("TDC2.0"));
        assertEquals(valueOf(200000000), us.parseObject("XBT2.0"));
        assertEquals(valueOf(200000000), usCoded.parseObject("฿ 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("Ƀ 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject(" 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("TDC 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("XBT 2"));
        assertEquals(valueOf(202222420000000L), us.parseObject("2,022,224.20"));
        assertEquals(valueOf(202222420000000L), us.parseObject("฿2,022,224.20"));
        assertEquals(valueOf(202222420000000L), us.parseObject("Ƀ2,022,224.20"));
        assertEquals(valueOf(202222420000000L), us.parseObject("TDC2,022,224.20"));
        assertEquals(valueOf(202222420000000L), us.parseObject("XBT2,022,224.20"));
        assertEquals(valueOf(220200000000L), us.parseObject("2,202.0"));
        assertEquals(valueOf(2100000000000000L), us.parseObject("21000000.00000000"));
        // MilliCoins
        assertEquals(valueOf(200000), usCoded.parseObject("mTDC 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("mXBT 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("m฿ 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("mɃ 2"));
        assertEquals(valueOf(200000), us.parseObject("mTDC2"));
        assertEquals(valueOf(200000), us.parseObject("mXBT2"));
        assertEquals(valueOf(200000), us.parseObject("₥฿2"));
        assertEquals(valueOf(200000), us.parseObject("₥Ƀ2"));
        assertEquals(valueOf(200000), us.parseObject("₥2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥TDC 2.00"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥XBT 2.00"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥TDC 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥XBT 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥฿ 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥Ƀ 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥ 2"));
        assertEquals(valueOf(202222400000L), us.parseObject("₥฿2,022,224"));
        assertEquals(valueOf(202222420000L), us.parseObject("₥Ƀ2,022,224.20"));
        assertEquals(valueOf(202222400000L), us.parseObject("m฿2,022,224"));
        assertEquals(valueOf(202222420000L), us.parseObject("mɃ2,022,224.20"));
        assertEquals(valueOf(202222400000L), us.parseObject("₥TDC2,022,224"));
        assertEquals(valueOf(202222400000L), us.parseObject("₥XBT2,022,224"));
        assertEquals(valueOf(202222400000L), us.parseObject("mTDC2,022,224"));
        assertEquals(valueOf(202222400000L), us.parseObject("mXBT2,022,224"));
        assertEquals(valueOf(202222420000L), us.parseObject("₥2,022,224.20"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("₥฿ 2,022,224"));
        assertEquals(valueOf(202222420000L), usCoded.parseObject("₥Ƀ 2,022,224.20"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("m฿ 2,022,224"));
        assertEquals(valueOf(202222420000L), usCoded.parseObject("mɃ 2,022,224.20"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("₥TDC 2,022,224"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("₥XBT 2,022,224"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("mTDC 2,022,224"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("mXBT 2,022,224"));
        assertEquals(valueOf(202222420000L), usCoded.parseObject("₥ 2,022,224.20"));
        // Microcoins
        assertEquals(valueOf(435), us.parseObject("µ฿4.35"));
        assertEquals(valueOf(435), us.parseObject("uɃ4.35"));
        assertEquals(valueOf(435), us.parseObject("u฿4.35"));
        assertEquals(valueOf(435), us.parseObject("µɃ4.35"));
        assertEquals(valueOf(435), us.parseObject("uTDC4.35"));
        assertEquals(valueOf(435), us.parseObject("uXBT4.35"));
        assertEquals(valueOf(435), us.parseObject("µTDC4.35"));
        assertEquals(valueOf(435), us.parseObject("µXBT4.35"));
        assertEquals(valueOf(435), usCoded.parseObject("uTDC 4.35"));
        assertEquals(valueOf(435), usCoded.parseObject("uXBT 4.35"));
        assertEquals(valueOf(435), usCoded.parseObject("µTDC 4.35"));
        assertEquals(valueOf(435), usCoded.parseObject("µXBT 4.35"));
        // fractional satoshi; round up
        assertEquals(valueOf(435), us.parseObject("uTDC4.345"));
        assertEquals(valueOf(435), us.parseObject("uXBT4.345"));
        // negative with mu symbol
        assertEquals(valueOf(-1), usCoded.parseObject("(µ฿ 0.01)"));
        assertEquals(valueOf(-10), us.parseObject("(µTDC0.100)"));
        assertEquals(valueOf(-10), us.parseObject("(µXBT0.100)"));

        // Same thing with addition of custom code, symbol
        us = TdcFormat.builder().locale(US).style(SYMBOL).symbol("£").code("XYZ").build();
        usCoded = TdcFormat.builder().locale(US).scale(0).symbol("£").code("XYZ").
                            pattern("¤ #,##0.00").build();
        // Coins
        assertEquals(valueOf(200000000), us.parseObject("XYZ2"));
        assertEquals(valueOf(200000000), us.parseObject("TDC2"));
        assertEquals(valueOf(200000000), us.parseObject("XBT2"));
        assertEquals(valueOf(200000000), us.parseObject("£2"));
        assertEquals(valueOf(200000000), us.parseObject("฿2"));
        assertEquals(valueOf(200000000), us.parseObject("Ƀ2"));
        assertEquals(valueOf(200000000), us.parseObject("2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("XYZ 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("TDC 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("XBT 2"));
        assertEquals(valueOf(200000000), us.parseObject("£2.0"));
        assertEquals(valueOf(200000000), us.parseObject("฿2.0"));
        assertEquals(valueOf(200000000), us.parseObject("Ƀ2.0"));
        assertEquals(valueOf(200000000), us.parseObject("2.0"));
        assertEquals(valueOf(200000000), us.parseObject("XYZ2.0"));
        assertEquals(valueOf(200000000), us.parseObject("TDC2.0"));
        assertEquals(valueOf(200000000), us.parseObject("XBT2.0"));
        assertEquals(valueOf(200000000), usCoded.parseObject("£ 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("฿ 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("Ƀ 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject(" 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("XYZ 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("TDC 2"));
        assertEquals(valueOf(200000000), usCoded.parseObject("XBT 2"));
        assertEquals(valueOf(202222420000000L), us.parseObject("2,022,224.20"));
        assertEquals(valueOf(202222420000000L), us.parseObject("£2,022,224.20"));
        assertEquals(valueOf(202222420000000L), us.parseObject("฿2,022,224.20"));
        assertEquals(valueOf(202222420000000L), us.parseObject("Ƀ2,022,224.20"));
        assertEquals(valueOf(202222420000000L), us.parseObject("XYZ2,022,224.20"));
        assertEquals(valueOf(202222420000000L), us.parseObject("TDC2,022,224.20"));
        assertEquals(valueOf(202222420000000L), us.parseObject("XBT2,022,224.20"));
        assertEquals(valueOf(220200000000L), us.parseObject("2,202.0"));
        assertEquals(valueOf(2100000000000000L), us.parseObject("21000000.00000000"));
        // MilliCoins
        assertEquals(valueOf(200000), usCoded.parseObject("mXYZ 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("mTDC 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("mXBT 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("m£ 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("m฿ 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("mɃ 2"));
        assertEquals(valueOf(200000), us.parseObject("mXYZ2"));
        assertEquals(valueOf(200000), us.parseObject("mTDC2"));
        assertEquals(valueOf(200000), us.parseObject("mXBT2"));
        assertEquals(valueOf(200000), us.parseObject("₥£2"));
        assertEquals(valueOf(200000), us.parseObject("₥฿2"));
        assertEquals(valueOf(200000), us.parseObject("₥Ƀ2"));
        assertEquals(valueOf(200000), us.parseObject("₥2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥XYZ 2.00"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥TDC 2.00"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥XBT 2.00"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥XYZ 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥TDC 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥XBT 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥£ 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥฿ 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥Ƀ 2"));
        assertEquals(valueOf(200000), usCoded.parseObject("₥ 2"));
        assertEquals(valueOf(202222400000L), us.parseObject("₥£2,022,224"));
        assertEquals(valueOf(202222400000L), us.parseObject("₥฿2,022,224"));
        assertEquals(valueOf(202222420000L), us.parseObject("₥Ƀ2,022,224.20"));
        assertEquals(valueOf(202222400000L), us.parseObject("m£2,022,224"));
        assertEquals(valueOf(202222400000L), us.parseObject("m฿2,022,224"));
        assertEquals(valueOf(202222420000L), us.parseObject("mɃ2,022,224.20"));
        assertEquals(valueOf(202222400000L), us.parseObject("₥XYZ2,022,224"));
        assertEquals(valueOf(202222400000L), us.parseObject("₥TDC2,022,224"));
        assertEquals(valueOf(202222400000L), us.parseObject("₥XBT2,022,224"));
        assertEquals(valueOf(202222400000L), us.parseObject("mXYZ2,022,224"));
        assertEquals(valueOf(202222400000L), us.parseObject("mTDC2,022,224"));
        assertEquals(valueOf(202222400000L), us.parseObject("mXBT2,022,224"));
        assertEquals(valueOf(202222420000L), us.parseObject("₥2,022,224.20"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("₥£ 2,022,224"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("₥฿ 2,022,224"));
        assertEquals(valueOf(202222420000L), usCoded.parseObject("₥Ƀ 2,022,224.20"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("m£ 2,022,224"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("m฿ 2,022,224"));
        assertEquals(valueOf(202222420000L), usCoded.parseObject("mɃ 2,022,224.20"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("₥XYZ 2,022,224"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("₥TDC 2,022,224"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("₥XBT 2,022,224"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("mXYZ 2,022,224"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("mTDC 2,022,224"));
        assertEquals(valueOf(202222400000L), usCoded.parseObject("mXBT 2,022,224"));
        assertEquals(valueOf(202222420000L), usCoded.parseObject("₥ 2,022,224.20"));
        // Microcoins
        assertEquals(valueOf(435), us.parseObject("µ£4.35"));
        assertEquals(valueOf(435), us.parseObject("µ฿4.35"));
        assertEquals(valueOf(435), us.parseObject("uɃ4.35"));
        assertEquals(valueOf(435), us.parseObject("u£4.35"));
        assertEquals(valueOf(435), us.parseObject("u฿4.35"));
        assertEquals(valueOf(435), us.parseObject("µɃ4.35"));
        assertEquals(valueOf(435), us.parseObject("uXYZ4.35"));
        assertEquals(valueOf(435), us.parseObject("uTDC4.35"));
        assertEquals(valueOf(435), us.parseObject("uXBT4.35"));
        assertEquals(valueOf(435), us.parseObject("µXYZ4.35"));
        assertEquals(valueOf(435), us.parseObject("µTDC4.35"));
        assertEquals(valueOf(435), us.parseObject("µXBT4.35"));
        assertEquals(valueOf(435), usCoded.parseObject("uXYZ 4.35"));
        assertEquals(valueOf(435), usCoded.parseObject("uTDC 4.35"));
        assertEquals(valueOf(435), usCoded.parseObject("uXBT 4.35"));
        assertEquals(valueOf(435), usCoded.parseObject("µXYZ 4.35"));
        assertEquals(valueOf(435), usCoded.parseObject("µTDC 4.35"));
        assertEquals(valueOf(435), usCoded.parseObject("µXBT 4.35"));
        // fractional satoshi; round up
        assertEquals(valueOf(435), us.parseObject("uXYZ4.345"));
        assertEquals(valueOf(435), us.parseObject("uTDC4.345"));
        assertEquals(valueOf(435), us.parseObject("uXBT4.345"));
        // negative with mu symbol
        assertEquals(valueOf(-1), usCoded.parseObject("µ£ -0.01"));
        assertEquals(valueOf(-1), usCoded.parseObject("µ฿ -0.01"));
        assertEquals(valueOf(-10), us.parseObject("(µXYZ0.100)"));
        assertEquals(valueOf(-10), us.parseObject("(µTDC0.100)"));
        assertEquals(valueOf(-10), us.parseObject("(µXBT0.100)"));

        // parse() method as opposed to parseObject
        try {
            TdcFormat.getInstance().parse("abc");
            fail("bad parse must raise exception");
        } catch (ParseException e) {}
    }

    @Test
    public void parseMetricTest() throws ParseException {
        TdcFormat cp = TdcFormat.getCodeInstance(Locale.US);
        TdcFormat sp = TdcFormat.getSymbolInstance(Locale.US);
        // coin
        assertEquals(parseCoin("1"), cp.parseObject("TDC 1.00"));
        assertEquals(parseCoin("1"), sp.parseObject("TDC1.00"));
        assertEquals(parseCoin("1"), cp.parseObject("฿ 1.00"));
        assertEquals(parseCoin("1"), sp.parseObject("฿1.00"));
        assertEquals(parseCoin("1"), cp.parseObject("B⃦ 1.00"));
        assertEquals(parseCoin("1"), sp.parseObject("B⃦1.00"));
        assertEquals(parseCoin("1"), cp.parseObject("Ƀ 1.00"));
        assertEquals(parseCoin("1"), sp.parseObject("Ƀ1.00"));
        // milli
        assertEquals(parseCoin("0.001"), cp.parseObject("mTDC 1.00"));
        assertEquals(parseCoin("0.001"), sp.parseObject("mTDC1.00"));
        assertEquals(parseCoin("0.001"), cp.parseObject("m฿ 1.00"));
        assertEquals(parseCoin("0.001"), sp.parseObject("m฿1.00"));
        assertEquals(parseCoin("0.001"), cp.parseObject("mB⃦ 1.00"));
        assertEquals(parseCoin("0.001"), sp.parseObject("mB⃦1.00"));
        assertEquals(parseCoin("0.001"), cp.parseObject("mɃ 1.00"));
        assertEquals(parseCoin("0.001"), sp.parseObject("mɃ1.00"));
        assertEquals(parseCoin("0.001"), cp.parseObject("₥TDC 1.00"));
        assertEquals(parseCoin("0.001"), sp.parseObject("₥TDC1.00"));
        assertEquals(parseCoin("0.001"), cp.parseObject("₥฿ 1.00"));
        assertEquals(parseCoin("0.001"), sp.parseObject("₥฿1.00"));
        assertEquals(parseCoin("0.001"), cp.parseObject("₥B⃦ 1.00"));
        assertEquals(parseCoin("0.001"), sp.parseObject("₥B⃦1.00"));
        assertEquals(parseCoin("0.001"), cp.parseObject("₥Ƀ 1.00"));
        assertEquals(parseCoin("0.001"), sp.parseObject("₥Ƀ1.00"));
        // micro
        assertEquals(parseCoin("0.000001"), cp.parseObject("uTDC 1.00"));
        assertEquals(parseCoin("0.000001"), sp.parseObject("uTDC1.00"));
        assertEquals(parseCoin("0.000001"), cp.parseObject("u฿ 1.00"));
        assertEquals(parseCoin("0.000001"), sp.parseObject("u฿1.00"));
        assertEquals(parseCoin("0.000001"), cp.parseObject("uB⃦ 1.00"));
        assertEquals(parseCoin("0.000001"), sp.parseObject("uB⃦1.00"));
        assertEquals(parseCoin("0.000001"), cp.parseObject("uɃ 1.00"));
        assertEquals(parseCoin("0.000001"), sp.parseObject("uɃ1.00"));
        assertEquals(parseCoin("0.000001"), cp.parseObject("µTDC 1.00"));
        assertEquals(parseCoin("0.000001"), sp.parseObject("µTDC1.00"));
        assertEquals(parseCoin("0.000001"), cp.parseObject("µ฿ 1.00"));
        assertEquals(parseCoin("0.000001"), sp.parseObject("µ฿1.00"));
        assertEquals(parseCoin("0.000001"), cp.parseObject("µB⃦ 1.00"));
        assertEquals(parseCoin("0.000001"), sp.parseObject("µB⃦1.00"));
        assertEquals(parseCoin("0.000001"), cp.parseObject("µɃ 1.00"));
        assertEquals(parseCoin("0.000001"), sp.parseObject("µɃ1.00"));
        // satoshi
        assertEquals(parseCoin("0.00000001"), cp.parseObject("uTDC 0.01"));
        assertEquals(parseCoin("0.00000001"), sp.parseObject("uTDC0.01"));
        assertEquals(parseCoin("0.00000001"), cp.parseObject("u฿ 0.01"));
        assertEquals(parseCoin("0.00000001"), sp.parseObject("u฿0.01"));
        assertEquals(parseCoin("0.00000001"), cp.parseObject("uB⃦ 0.01"));
        assertEquals(parseCoin("0.00000001"), sp.parseObject("uB⃦0.01"));
        assertEquals(parseCoin("0.00000001"), cp.parseObject("uɃ 0.01"));
        assertEquals(parseCoin("0.00000001"), sp.parseObject("uɃ0.01"));
        assertEquals(parseCoin("0.00000001"), cp.parseObject("µTDC 0.01"));
        assertEquals(parseCoin("0.00000001"), sp.parseObject("µTDC0.01"));
        assertEquals(parseCoin("0.00000001"), cp.parseObject("µ฿ 0.01"));
        assertEquals(parseCoin("0.00000001"), sp.parseObject("µ฿0.01"));
        assertEquals(parseCoin("0.00000001"), cp.parseObject("µB⃦ 0.01"));
        assertEquals(parseCoin("0.00000001"), sp.parseObject("µB⃦0.01"));
        assertEquals(parseCoin("0.00000001"), cp.parseObject("µɃ 0.01"));
        assertEquals(parseCoin("0.00000001"), sp.parseObject("µɃ0.01"));
        // cents
        assertEquals(parseCoin("0.01234567"), cp.parseObject("cTDC 1.234567"));
        assertEquals(parseCoin("0.01234567"), sp.parseObject("cTDC1.234567"));
        assertEquals(parseCoin("0.01234567"), cp.parseObject("c฿ 1.234567"));
        assertEquals(parseCoin("0.01234567"), sp.parseObject("c฿1.234567"));
        assertEquals(parseCoin("0.01234567"), cp.parseObject("cB⃦ 1.234567"));
        assertEquals(parseCoin("0.01234567"), sp.parseObject("cB⃦1.234567"));
        assertEquals(parseCoin("0.01234567"), cp.parseObject("cɃ 1.234567"));
        assertEquals(parseCoin("0.01234567"), sp.parseObject("cɃ1.234567"));
        assertEquals(parseCoin("0.01234567"), cp.parseObject("¢TDC 1.234567"));
        assertEquals(parseCoin("0.01234567"), sp.parseObject("¢TDC1.234567"));
        assertEquals(parseCoin("0.01234567"), cp.parseObject("¢฿ 1.234567"));
        assertEquals(parseCoin("0.01234567"), sp.parseObject("¢฿1.234567"));
        assertEquals(parseCoin("0.01234567"), cp.parseObject("¢B⃦ 1.234567"));
        assertEquals(parseCoin("0.01234567"), sp.parseObject("¢B⃦1.234567"));
        assertEquals(parseCoin("0.01234567"), cp.parseObject("¢Ƀ 1.234567"));
        assertEquals(parseCoin("0.01234567"), sp.parseObject("¢Ƀ1.234567"));
        // dekacoins
        assertEquals(parseCoin("12.34567"), cp.parseObject("daTDC 1.234567"));
        assertEquals(parseCoin("12.34567"), sp.parseObject("daTDC1.234567"));
        assertEquals(parseCoin("12.34567"), cp.parseObject("da฿ 1.234567"));
        assertEquals(parseCoin("12.34567"), sp.parseObject("da฿1.234567"));
        assertEquals(parseCoin("12.34567"), cp.parseObject("daB⃦ 1.234567"));
        assertEquals(parseCoin("12.34567"), sp.parseObject("daB⃦1.234567"));
        assertEquals(parseCoin("12.34567"), cp.parseObject("daɃ 1.234567"));
        assertEquals(parseCoin("12.34567"), sp.parseObject("daɃ1.234567"));
        // hectocoins
        assertEquals(parseCoin("123.4567"), cp.parseObject("hTDC 1.234567"));
        assertEquals(parseCoin("123.4567"), sp.parseObject("hTDC1.234567"));
        assertEquals(parseCoin("123.4567"), cp.parseObject("h฿ 1.234567"));
        assertEquals(parseCoin("123.4567"), sp.parseObject("h฿1.234567"));
        assertEquals(parseCoin("123.4567"), cp.parseObject("hB⃦ 1.234567"));
        assertEquals(parseCoin("123.4567"), sp.parseObject("hB⃦1.234567"));
        assertEquals(parseCoin("123.4567"), cp.parseObject("hɃ 1.234567"));
        assertEquals(parseCoin("123.4567"), sp.parseObject("hɃ1.234567"));
        // kilocoins
        assertEquals(parseCoin("1234.567"), cp.parseObject("kTDC 1.234567"));
        assertEquals(parseCoin("1234.567"), sp.parseObject("kTDC1.234567"));
        assertEquals(parseCoin("1234.567"), cp.parseObject("k฿ 1.234567"));
        assertEquals(parseCoin("1234.567"), sp.parseObject("k฿1.234567"));
        assertEquals(parseCoin("1234.567"), cp.parseObject("kB⃦ 1.234567"));
        assertEquals(parseCoin("1234.567"), sp.parseObject("kB⃦1.234567"));
        assertEquals(parseCoin("1234.567"), cp.parseObject("kɃ 1.234567"));
        assertEquals(parseCoin("1234.567"), sp.parseObject("kɃ1.234567"));
        // megacoins
        assertEquals(parseCoin("1234567"), cp.parseObject("MTDC 1.234567"));
        assertEquals(parseCoin("1234567"), sp.parseObject("MTDC1.234567"));
        assertEquals(parseCoin("1234567"), cp.parseObject("M฿ 1.234567"));
        assertEquals(parseCoin("1234567"), sp.parseObject("M฿1.234567"));
        assertEquals(parseCoin("1234567"), cp.parseObject("MB⃦ 1.234567"));
        assertEquals(parseCoin("1234567"), sp.parseObject("MB⃦1.234567"));
        assertEquals(parseCoin("1234567"), cp.parseObject("MɃ 1.234567"));
        assertEquals(parseCoin("1234567"), sp.parseObject("MɃ1.234567"));
    }

    @Test
    public void parsePositionTest() {
        TdcFormat usCoded = TdcFormat.getCodeInstance(Locale.US);
        // Test the field constants
        FieldPosition intField = new FieldPosition(NumberFormat.Field.INTEGER);
        assertEquals(
          "987,654,321",
          usCoded.format(valueOf(98765432123L), new StringBuffer(), intField).
          substring(intField.getBeginIndex(), intField.getEndIndex())
        );
        FieldPosition fracField = new FieldPosition(NumberFormat.Field.FRACTION);
        assertEquals(
          "23",
          usCoded.format(valueOf(98765432123L), new StringBuffer(), fracField).
          substring(fracField.getBeginIndex(), fracField.getEndIndex())
        );

        // for currency we use a locale that puts the units at the end
        TdcFormat de = TdcFormat.getSymbolInstance(Locale.GERMANY);
        TdcFormat deCoded = TdcFormat.getCodeInstance(Locale.GERMANY);
        FieldPosition currField = new FieldPosition(NumberFormat.Field.CURRENCY);
        assertEquals(
          "µ฿",
          de.format(valueOf(98765432123L), new StringBuffer(), currField).
          substring(currField.getBeginIndex(), currField.getEndIndex())
        );
        assertEquals(
          "µTDC",
          deCoded.format(valueOf(98765432123L), new StringBuffer(), currField).
          substring(currField.getBeginIndex(), currField.getEndIndex())
        );
        assertEquals(
          "₥฿",
          de.format(valueOf(98765432000L), new StringBuffer(), currField).
          substring(currField.getBeginIndex(), currField.getEndIndex())
        );
        assertEquals(
          "mTDC",
          deCoded.format(valueOf(98765432000L), new StringBuffer(), currField).
          substring(currField.getBeginIndex(), currField.getEndIndex())
        );
        assertEquals(
          "฿",
          de.format(valueOf(98765000000L), new StringBuffer(), currField).
          substring(currField.getBeginIndex(), currField.getEndIndex())
        );
        assertEquals(
          "TDC",
          deCoded.format(valueOf(98765000000L), new StringBuffer(), currField).
          substring(currField.getBeginIndex(), currField.getEndIndex())
        );
    }

    @Ignore("non-determinism between OpenJDK versions")
    @Test
    public void currencyCodeTest() {
        /* Insert needed space AFTER currency-code */
        TdcFormat usCoded = TdcFormat.getCodeInstance(Locale.US);
        assertEquals("µTDC 0.01", usCoded.format(1));
        assertEquals("TDC 1.00", usCoded.format(COIN));

        /* Do not insert unneeded space BEFORE currency-code */
        TdcFormat frCoded = TdcFormat.getCodeInstance(Locale.FRANCE);
        assertEquals("0,01 µTDC", frCoded.format(1));
        assertEquals("1,00 TDC", frCoded.format(COIN));

        /* Insert needed space BEFORE currency-code: no known currency pattern does this? */

        /* Do not insert unneeded space AFTER currency-code */
        TdcFormat deCoded = TdcFormat.getCodeInstance(Locale.ITALY);
        assertEquals("µTDC 0,01", deCoded.format(1));
        assertEquals("TDC 1,00", deCoded.format(COIN));
    }

    @Test
    public void coinScaleTest() throws Exception {
        TdcFormat coinFormat = TdcFormat.getCoinInstance(Locale.US);
        assertEquals("1.00", coinFormat.format(Coin.COIN));
        assertEquals("-1.00", coinFormat.format(Coin.COIN.negate()));
        assertEquals(Coin.parseCoin("1"), coinFormat.parseObject("1.00"));
        assertEquals(valueOf(1000000), coinFormat.parseObject("0.01"));
        assertEquals(Coin.parseCoin("1000"), coinFormat.parseObject("1,000.00"));
        assertEquals(Coin.parseCoin("1000"), coinFormat.parseObject("1000"));
    }

    @Test
    public void millicoinScaleTest() throws Exception {
        TdcFormat coinFormat = TdcFormat.getMilliInstance(Locale.US);
        assertEquals("1,000.00", coinFormat.format(Coin.COIN));
        assertEquals("-1,000.00", coinFormat.format(Coin.COIN.negate()));
        assertEquals(Coin.parseCoin("0.001"), coinFormat.parseObject("1.00"));
        assertEquals(valueOf(1000), coinFormat.parseObject("0.01"));
        assertEquals(Coin.parseCoin("1"), coinFormat.parseObject("1,000.00"));
        assertEquals(Coin.parseCoin("1"), coinFormat.parseObject("1000"));
    }

    @Test
    public void microcoinScaleTest() throws Exception {
        TdcFormat coinFormat = TdcFormat.getMicroInstance(Locale.US);
        assertEquals("1,000,000.00", coinFormat.format(Coin.COIN));
        assertEquals("-1,000,000.00", coinFormat.format(Coin.COIN.negate()));
        assertEquals("1,000,000.10", coinFormat.format(Coin.COIN.add(valueOf(10))));
        assertEquals(Coin.parseCoin("0.000001"), coinFormat.parseObject("1.00"));
        assertEquals(valueOf(1), coinFormat.parseObject("0.01"));
        assertEquals(Coin.parseCoin("0.001"), coinFormat.parseObject("1,000.00"));
        assertEquals(Coin.parseCoin("0.001"), coinFormat.parseObject("1000"));
    }

    @Test
    public void testGrouping() throws Exception {
        TdcFormat usCoin = TdcFormat.getInstance(0, Locale.US, 1, 2, 3);
        assertEquals("0.1", usCoin.format(Coin.parseCoin("0.1")));
        assertEquals("0.010", usCoin.format(Coin.parseCoin("0.01")));
        assertEquals("0.001", usCoin.format(Coin.parseCoin("0.001")));
        assertEquals("0.000100", usCoin.format(Coin.parseCoin("0.0001")));
        assertEquals("0.000010", usCoin.format(Coin.parseCoin("0.00001")));
        assertEquals("0.000001", usCoin.format(Coin.parseCoin("0.000001")));

        // no more than two fractional decimal places for the default coin-denomination
        assertEquals("0.01", TdcFormat.getCoinInstance(Locale.US).format(Coin.parseCoin("0.005")));

        TdcFormat usMilli = TdcFormat.getInstance(3, Locale.US, 1, 2, 3);
        assertEquals("0.1", usMilli.format(Coin.parseCoin("0.0001")));
        assertEquals("0.010", usMilli.format(Coin.parseCoin("0.00001")));
        assertEquals("0.001", usMilli.format(Coin.parseCoin("0.000001")));
        // even though last group is 3, that would result in fractional satoshis, which we don't do
        assertEquals("0.00010", usMilli.format(Coin.valueOf(10)));
        assertEquals("0.00001", usMilli.format(Coin.valueOf(1)));

        TdcFormat usMicro = TdcFormat.getInstance(6, Locale.US, 1, 2, 3);
        assertEquals("0.1", usMicro.format(Coin.valueOf(10)));
        // even though second group is 2, that would result in fractional satoshis, which we don't do
        assertEquals("0.01", usMicro.format(Coin.valueOf(1)));
    }


    /* These just make sure factory methods don't raise exceptions.
     * Other tests inspect their return values. */
    @Test
    public void factoryTest() {
        TdcFormat coded = TdcFormat.getInstance(0, 1, 2, 3);
        TdcFormat.getInstance(TdcAutoFormat.Style.CODE);
        TdcAutoFormat symbolic = (TdcAutoFormat)TdcFormat.getInstance(TdcAutoFormat.Style.SYMBOL);
        assertEquals(2, symbolic.fractionPlaces());
        TdcFormat.getInstance(TdcAutoFormat.Style.CODE, 3);
        assertEquals(3, ((TdcAutoFormat)TdcFormat.getInstance(TdcAutoFormat.Style.SYMBOL, 3)).fractionPlaces());
        TdcFormat.getInstance(TdcAutoFormat.Style.SYMBOL, Locale.US, 3);
        TdcFormat.getInstance(TdcAutoFormat.Style.CODE, Locale.US);
        TdcFormat.getInstance(TdcAutoFormat.Style.SYMBOL, Locale.US);
        TdcFormat.getCoinInstance(2, TdcFixedFormat.REPEATING_PLACES);
        TdcFormat.getMilliInstance(1, 2, 3);
        TdcFormat.getInstance(2);
        TdcFormat.getInstance(2, Locale.US);
        TdcFormat.getCodeInstance(3);
        TdcFormat.getSymbolInstance(3);
        TdcFormat.getCodeInstance(Locale.US, 3);
        TdcFormat.getSymbolInstance(Locale.US, 3);
        try {
            TdcFormat.getInstance(SMALLEST_UNIT_EXPONENT + 1);
            fail("should not have constructed an instance with denomination less than satoshi");
        } catch (IllegalArgumentException e) {}
    }
    @Test
    public void factoryArgumentsTest() {
        Locale locale;
        if (Locale.getDefault().equals(GERMANY)) locale = FRANCE;
        else locale = GERMANY;
        assertEquals(TdcFormat.getInstance(), TdcFormat.getCodeInstance());
        assertEquals(TdcFormat.getInstance(locale), TdcFormat.getCodeInstance(locale));
        assertEquals(TdcFormat.getInstance(TdcAutoFormat.Style.CODE), TdcFormat.getCodeInstance());
        assertEquals(TdcFormat.getInstance(TdcAutoFormat.Style.SYMBOL), TdcFormat.getSymbolInstance());
        assertEquals(TdcFormat.getInstance(TdcAutoFormat.Style.CODE,3), TdcFormat.getCodeInstance(3));
        assertEquals(TdcFormat.getInstance(TdcAutoFormat.Style.SYMBOL,3), TdcFormat.getSymbolInstance(3));
        assertEquals(TdcFormat.getInstance(TdcAutoFormat.Style.CODE,locale), TdcFormat.getCodeInstance(locale));
        assertEquals(TdcFormat.getInstance(TdcAutoFormat.Style.SYMBOL,locale), TdcFormat.getSymbolInstance(locale));
        assertEquals(TdcFormat.getInstance(TdcAutoFormat.Style.CODE,locale,3), TdcFormat.getCodeInstance(locale,3));
        assertEquals(TdcFormat.getInstance(TdcAutoFormat.Style.SYMBOL,locale,3), TdcFormat.getSymbolInstance(locale,3));
        assertEquals(TdcFormat.getCoinInstance(), TdcFormat.getInstance(0));
        assertEquals(TdcFormat.getMilliInstance(), TdcFormat.getInstance(3));
        assertEquals(TdcFormat.getMicroInstance(), TdcFormat.getInstance(6));
        assertEquals(TdcFormat.getCoinInstance(3), TdcFormat.getInstance(0,3));
        assertEquals(TdcFormat.getMilliInstance(3), TdcFormat.getInstance(3,3));
        assertEquals(TdcFormat.getMicroInstance(3), TdcFormat.getInstance(6,3));
        assertEquals(TdcFormat.getCoinInstance(3,4,5), TdcFormat.getInstance(0,3,4,5));
        assertEquals(TdcFormat.getMilliInstance(3,4,5), TdcFormat.getInstance(3,3,4,5));
        assertEquals(TdcFormat.getMicroInstance(3,4,5), TdcFormat.getInstance(6,3,4,5));
        assertEquals(TdcFormat.getCoinInstance(locale), TdcFormat.getInstance(0,locale));
        assertEquals(TdcFormat.getMilliInstance(locale), TdcFormat.getInstance(3,locale));
        assertEquals(TdcFormat.getMicroInstance(locale), TdcFormat.getInstance(6,locale));
        assertEquals(TdcFormat.getCoinInstance(locale,4,5), TdcFormat.getInstance(0,locale,4,5));
        assertEquals(TdcFormat.getMilliInstance(locale,4,5), TdcFormat.getInstance(3,locale,4,5));
        assertEquals(TdcFormat.getMicroInstance(locale,4,5), TdcFormat.getInstance(6,locale,4,5));
    }

    @Test
    public void autoDecimalTest() {
        TdcFormat codedZero = TdcFormat.getCodeInstance(Locale.US, 0);
        TdcFormat symbolZero = TdcFormat.getSymbolInstance(Locale.US, 0);
        assertEquals("฿1", symbolZero.format(COIN));
        assertEquals("TDC 1", codedZero.format(COIN));
        assertEquals("µ฿1,000,000", symbolZero.format(COIN.subtract(SATOSHI)));
        assertEquals("µTDC 1,000,000", codedZero.format(COIN.subtract(SATOSHI)));
        assertEquals("µ฿1,000,000", symbolZero.format(COIN.subtract(Coin.valueOf(50))));
        assertEquals("µTDC 1,000,000", codedZero.format(COIN.subtract(Coin.valueOf(50))));
        assertEquals("µ฿999,999", symbolZero.format(COIN.subtract(Coin.valueOf(51))));
        assertEquals("µTDC 999,999", codedZero.format(COIN.subtract(Coin.valueOf(51))));
        assertEquals("฿1,000", symbolZero.format(COIN.multiply(1000)));
        assertEquals("TDC 1,000", codedZero.format(COIN.multiply(1000)));
        assertEquals("µ฿1", symbolZero.format(Coin.valueOf(100)));
        assertEquals("µTDC 1", codedZero.format(Coin.valueOf(100)));
        assertEquals("µ฿1", symbolZero.format(Coin.valueOf(50)));
        assertEquals("µTDC 1", codedZero.format(Coin.valueOf(50)));
        assertEquals("µ฿0", symbolZero.format(Coin.valueOf(49)));
        assertEquals("µTDC 0", codedZero.format(Coin.valueOf(49)));
        assertEquals("µ฿0", symbolZero.format(Coin.valueOf(1)));
        assertEquals("µTDC 0", codedZero.format(Coin.valueOf(1)));
        assertEquals("µ฿500,000", symbolZero.format(Coin.valueOf(49999999)));
        assertEquals("µTDC 500,000", codedZero.format(Coin.valueOf(49999999)));

        assertEquals("µ฿499,500", symbolZero.format(Coin.valueOf(49950000)));
        assertEquals("µTDC 499,500", codedZero.format(Coin.valueOf(49950000)));
        assertEquals("µ฿499,500", symbolZero.format(Coin.valueOf(49949999)));
        assertEquals("µTDC 499,500", codedZero.format(Coin.valueOf(49949999)));
        assertEquals("µ฿500,490", symbolZero.format(Coin.valueOf(50049000)));
        assertEquals("µTDC 500,490", codedZero.format(Coin.valueOf(50049000)));
        assertEquals("µ฿500,490", symbolZero.format(Coin.valueOf(50049001)));
        assertEquals("µTDC 500,490", codedZero.format(Coin.valueOf(50049001)));
        assertEquals("µ฿500,000", symbolZero.format(Coin.valueOf(49999950)));
        assertEquals("µTDC 500,000", codedZero.format(Coin.valueOf(49999950)));
        assertEquals("µ฿499,999", symbolZero.format(Coin.valueOf(49999949)));
        assertEquals("µTDC 499,999", codedZero.format(Coin.valueOf(49999949)));
        assertEquals("µ฿500,000", symbolZero.format(Coin.valueOf(50000049)));
        assertEquals("µTDC 500,000", codedZero.format(Coin.valueOf(50000049)));
        assertEquals("µ฿500,001", symbolZero.format(Coin.valueOf(50000050)));
        assertEquals("µTDC 500,001", codedZero.format(Coin.valueOf(50000050)));

        TdcFormat codedTwo = TdcFormat.getCodeInstance(Locale.US, 2);
        TdcFormat symbolTwo = TdcFormat.getSymbolInstance(Locale.US, 2);
        assertEquals("฿1.00", symbolTwo.format(COIN));
        assertEquals("TDC 1.00", codedTwo.format(COIN));
        assertEquals("µ฿999,999.99", symbolTwo.format(COIN.subtract(SATOSHI)));
        assertEquals("µTDC 999,999.99", codedTwo.format(COIN.subtract(SATOSHI)));
        assertEquals("฿1,000.00", symbolTwo.format(COIN.multiply(1000)));
        assertEquals("TDC 1,000.00", codedTwo.format(COIN.multiply(1000)));
        assertEquals("µ฿1.00", symbolTwo.format(Coin.valueOf(100)));
        assertEquals("µTDC 1.00", codedTwo.format(Coin.valueOf(100)));
        assertEquals("µ฿0.50", symbolTwo.format(Coin.valueOf(50)));
        assertEquals("µTDC 0.50", codedTwo.format(Coin.valueOf(50)));
        assertEquals("µ฿0.49", symbolTwo.format(Coin.valueOf(49)));
        assertEquals("µTDC 0.49", codedTwo.format(Coin.valueOf(49)));
        assertEquals("µ฿0.01", symbolTwo.format(Coin.valueOf(1)));
        assertEquals("µTDC 0.01", codedTwo.format(Coin.valueOf(1)));

        TdcFormat codedThree = TdcFormat.getCodeInstance(Locale.US, 3);
        TdcFormat symbolThree = TdcFormat.getSymbolInstance(Locale.US, 3);
        assertEquals("฿1.000", symbolThree.format(COIN));
        assertEquals("TDC 1.000", codedThree.format(COIN));
        assertEquals("µ฿999,999.99", symbolThree.format(COIN.subtract(SATOSHI)));
        assertEquals("µTDC 999,999.99", codedThree.format(COIN.subtract(SATOSHI)));
        assertEquals("฿1,000.000", symbolThree.format(COIN.multiply(1000)));
        assertEquals("TDC 1,000.000", codedThree.format(COIN.multiply(1000)));
        assertEquals("₥฿0.001", symbolThree.format(Coin.valueOf(100)));
        assertEquals("mTDC 0.001", codedThree.format(Coin.valueOf(100)));
        assertEquals("µ฿0.50", symbolThree.format(Coin.valueOf(50)));
        assertEquals("µTDC 0.50", codedThree.format(Coin.valueOf(50)));
        assertEquals("µ฿0.49", symbolThree.format(Coin.valueOf(49)));
        assertEquals("µTDC 0.49", codedThree.format(Coin.valueOf(49)));
        assertEquals("µ฿0.01", symbolThree.format(Coin.valueOf(1)));
        assertEquals("µTDC 0.01", codedThree.format(Coin.valueOf(1)));
    }


    @Test
    public void symbolsCodesTest() {
        TdcFixedFormat coin = (TdcFixedFormat)TdcFormat.getCoinInstance(US);
        assertEquals("TDC", coin.code());
        assertEquals("฿", coin.symbol());
        TdcFixedFormat cent = (TdcFixedFormat)TdcFormat.getInstance(2, US);
        assertEquals("cTDC", cent.code());
        assertEquals("¢฿", cent.symbol());
        TdcFixedFormat milli = (TdcFixedFormat)TdcFormat.getInstance(3, US);
        assertEquals("mTDC", milli.code());
        assertEquals("₥฿", milli.symbol());
        TdcFixedFormat micro = (TdcFixedFormat)TdcFormat.getInstance(6, US);
        assertEquals("µTDC", micro.code());
        assertEquals("µ฿", micro.symbol());
        TdcFixedFormat deka = (TdcFixedFormat)TdcFormat.getInstance(-1, US);
        assertEquals("daTDC", deka.code());
        assertEquals("da฿", deka.symbol());
        TdcFixedFormat hecto = (TdcFixedFormat)TdcFormat.getInstance(-2, US);
        assertEquals("hTDC", hecto.code());
        assertEquals("h฿", hecto.symbol());
        TdcFixedFormat kilo = (TdcFixedFormat)TdcFormat.getInstance(-3, US);
        assertEquals("kTDC", kilo.code());
        assertEquals("k฿", kilo.symbol());
        TdcFixedFormat mega = (TdcFixedFormat)TdcFormat.getInstance(-6, US);
        assertEquals("MTDC", mega.code());
        assertEquals("M฿", mega.symbol());
        TdcFixedFormat noSymbol = (TdcFixedFormat)TdcFormat.getInstance(4, US);
        try {
            noSymbol.symbol();
            fail("non-standard denomination has no symbol()");
        } catch (IllegalStateException e) {}
        try {
            noSymbol.code();
            fail("non-standard denomination has no code()");
        } catch (IllegalStateException e) {}

        TdcFixedFormat symbolCoin = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(0).
                                                              symbol("B\u20e6").build();
        assertEquals("TDC", symbolCoin.code());
        assertEquals("B⃦", symbolCoin.symbol());
        TdcFixedFormat symbolCent = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(2).
                                                              symbol("B\u20e6").build();
        assertEquals("cTDC", symbolCent.code());
        assertEquals("¢B⃦", symbolCent.symbol());
        TdcFixedFormat symbolMilli = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(3).
                                                               symbol("B\u20e6").build();
        assertEquals("mTDC", symbolMilli.code());
        assertEquals("₥B⃦", symbolMilli.symbol());
        TdcFixedFormat symbolMicro = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(6).
                                                               symbol("B\u20e6").build();
        assertEquals("µTDC", symbolMicro.code());
        assertEquals("µB⃦", symbolMicro.symbol());
        TdcFixedFormat symbolDeka = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(-1).
                                                              symbol("B\u20e6").build();
        assertEquals("daTDC", symbolDeka.code());
        assertEquals("daB⃦", symbolDeka.symbol());
        TdcFixedFormat symbolHecto = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(-2).
                                                               symbol("B\u20e6").build();
        assertEquals("hTDC", symbolHecto.code());
        assertEquals("hB⃦", symbolHecto.symbol());
        TdcFixedFormat symbolKilo = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(-3).
                                                              symbol("B\u20e6").build();
        assertEquals("kTDC", symbolKilo.code());
        assertEquals("kB⃦", symbolKilo.symbol());
        TdcFixedFormat symbolMega = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(-6).
                                                              symbol("B\u20e6").build();
        assertEquals("MTDC", symbolMega.code());
        assertEquals("MB⃦", symbolMega.symbol());

        TdcFixedFormat codeCoin = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(0).
                                                            code("XBT").build();
        assertEquals("XBT", codeCoin.code());
        assertEquals("฿", codeCoin.symbol());
        TdcFixedFormat codeCent = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(2).
                                                            code("XBT").build();
        assertEquals("cXBT", codeCent.code());
        assertEquals("¢฿", codeCent.symbol());
        TdcFixedFormat codeMilli = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(3).
                                                             code("XBT").build();
        assertEquals("mXBT", codeMilli.code());
        assertEquals("₥฿", codeMilli.symbol());
        TdcFixedFormat codeMicro = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(6).
                                                             code("XBT").build();
        assertEquals("µXBT", codeMicro.code());
        assertEquals("µ฿", codeMicro.symbol());
        TdcFixedFormat codeDeka = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(-1).
                                                            code("XBT").build();
        assertEquals("daXBT", codeDeka.code());
        assertEquals("da฿", codeDeka.symbol());
        TdcFixedFormat codeHecto = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(-2).
                                                             code("XBT").build();
        assertEquals("hXBT", codeHecto.code());
        assertEquals("h฿", codeHecto.symbol());
        TdcFixedFormat codeKilo = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(-3).
                                                            code("XBT").build();
        assertEquals("kXBT", codeKilo.code());
        assertEquals("k฿", codeKilo.symbol());
        TdcFixedFormat codeMega = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(-6).
                                                            code("XBT").build();
        assertEquals("MXBT", codeMega.code());
        assertEquals("M฿", codeMega.symbol());

        TdcFixedFormat symbolCodeCoin = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(0).
                                                                  symbol("B\u20e6").code("XBT").build();
        assertEquals("XBT", symbolCodeCoin.code());
        assertEquals("B⃦", symbolCodeCoin.symbol());
        TdcFixedFormat symbolCodeCent = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(2).
                                                                  symbol("B\u20e6").code("XBT").build();
        assertEquals("cXBT", symbolCodeCent.code());
        assertEquals("¢B⃦", symbolCodeCent.symbol());
        TdcFixedFormat symbolCodeMilli = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(3).
                                                                   symbol("B\u20e6").code("XBT").build();
        assertEquals("mXBT", symbolCodeMilli.code());
        assertEquals("₥B⃦", symbolCodeMilli.symbol());
        TdcFixedFormat symbolCodeMicro = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(6).
                                                                   symbol("B\u20e6").code("XBT").build();
        assertEquals("µXBT", symbolCodeMicro.code());
        assertEquals("µB⃦", symbolCodeMicro.symbol());
        TdcFixedFormat symbolCodeDeka = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(-1).
                                                                  symbol("B\u20e6").code("XBT").build();
        assertEquals("daXBT", symbolCodeDeka.code());
        assertEquals("daB⃦", symbolCodeDeka.symbol());
        TdcFixedFormat symbolCodeHecto = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(-2).
                                                                   symbol("B\u20e6").code("XBT").build();
        assertEquals("hXBT", symbolCodeHecto.code());
        assertEquals("hB⃦", symbolCodeHecto.symbol());
        TdcFixedFormat symbolCodeKilo = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(-3).
                                                                  symbol("B\u20e6").code("XBT").build();
        assertEquals("kXBT", symbolCodeKilo.code());
        assertEquals("kB⃦", symbolCodeKilo.symbol());
        TdcFixedFormat symbolCodeMega = (TdcFixedFormat)TdcFormat.builder().locale(US).scale(-6).
                                                                  symbol("B\u20e6").code("XBT").build();
        assertEquals("MXBT", symbolCodeMega.code());
        assertEquals("MB⃦", symbolCodeMega.symbol());
    }

    /* copied from CoinFormatTest.java and modified */
    @Test
    public void parse() throws Exception {
        TdcFormat coin = TdcFormat.getCoinInstance(Locale.US);
        assertEquals(Coin.COIN, coin.parseObject("1"));
        assertEquals(Coin.COIN, coin.parseObject("1."));
        assertEquals(Coin.COIN, coin.parseObject("1.0"));
        assertEquals(Coin.COIN, TdcFormat.getCoinInstance(Locale.GERMANY).parseObject("1,0"));
        assertEquals(Coin.COIN, coin.parseObject("01.0000000000"));
        // TODO work with express positive sign
        // assertEquals(Coin.COIN, coin.parseObject("+1.0"));
        assertEquals(Coin.COIN.negate(), coin.parseObject("-1"));
        assertEquals(Coin.COIN.negate(), coin.parseObject("-1.0"));

        assertEquals(Coin.CENT, coin.parseObject(".01"));

        TdcFormat milli = TdcFormat.getMilliInstance(Locale.US);
        assertEquals(Coin.MILLICOIN, milli.parseObject("1"));
        assertEquals(Coin.MILLICOIN, milli.parseObject("1.0"));
        assertEquals(Coin.MILLICOIN, milli.parseObject("01.0000000000"));
        // TODO work with express positive sign
        //assertEquals(Coin.MILLICOIN, milli.parseObject("+1.0"));
        assertEquals(Coin.MILLICOIN.negate(), milli.parseObject("-1"));
        assertEquals(Coin.MILLICOIN.negate(), milli.parseObject("-1.0"));

        TdcFormat micro = TdcFormat.getMicroInstance(Locale.US);
        assertEquals(Coin.MICROCOIN, micro.parseObject("1"));
        assertEquals(Coin.MICROCOIN, micro.parseObject("1.0"));
        assertEquals(Coin.MICROCOIN, micro.parseObject("01.0000000000"));
        // TODO work with express positive sign
        // assertEquals(Coin.MICROCOIN, micro.parseObject("+1.0"));
        assertEquals(Coin.MICROCOIN.negate(), micro.parseObject("-1"));
        assertEquals(Coin.MICROCOIN.negate(), micro.parseObject("-1.0"));
    }

    /* Copied (and modified) from CoinFormatTest.java */
    @Test
    public void tdcRounding() throws Exception {
        TdcFormat coinFormat = TdcFormat.getCoinInstance(Locale.US);
        assertEquals("0", TdcFormat.getCoinInstance(Locale.US, 0).format(ZERO));
        assertEquals("0", coinFormat.format(ZERO, 0));
        assertEquals("0.00", TdcFormat.getCoinInstance(Locale.US, 2).format(ZERO));
        assertEquals("0.00", coinFormat.format(ZERO, 2));

        assertEquals("1", TdcFormat.getCoinInstance(Locale.US, 0).format(COIN));
        assertEquals("1", coinFormat.format(COIN, 0));
        assertEquals("1.0", TdcFormat.getCoinInstance(Locale.US, 1).format(COIN));
        assertEquals("1.0", coinFormat.format(COIN, 1));
        assertEquals("1.00", TdcFormat.getCoinInstance(Locale.US, 2, 2).format(COIN));
        assertEquals("1.00", coinFormat.format(COIN, 2, 2));
        assertEquals("1.00", TdcFormat.getCoinInstance(Locale.US, 2, 2, 2).format(COIN));
        assertEquals("1.00", coinFormat.format(COIN, 2, 2, 2));
        assertEquals("1.00", TdcFormat.getCoinInstance(Locale.US, 2, 2, 2, 2).format(COIN));
        assertEquals("1.00", coinFormat.format(COIN, 2, 2, 2, 2));
        assertEquals("1.000", TdcFormat.getCoinInstance(Locale.US, 3).format(COIN));
        assertEquals("1.000", coinFormat.format(COIN, 3));
        assertEquals("1.0000", TdcFormat.getCoinInstance(US, 4).format(COIN));
        assertEquals("1.0000", coinFormat.format(COIN, 4));

        final Coin justNot = COIN.subtract(SATOSHI);
        assertEquals("1", TdcFormat.getCoinInstance(US, 0).format(justNot));
        assertEquals("1", coinFormat.format(justNot, 0));
        assertEquals("1.0", TdcFormat.getCoinInstance(US, 1).format(justNot));
        assertEquals("1.0", coinFormat.format(justNot, 1));
        final Coin justNotUnder = Coin.valueOf(99995000);
        assertEquals("1.00", TdcFormat.getCoinInstance(US, 2, 2).format(justNot));
        assertEquals("1.00", coinFormat.format(justNot, 2, 2));
        assertEquals("1.00", TdcFormat.getCoinInstance(US, 2, 2).format(justNotUnder));
        assertEquals("1.00", coinFormat.format(justNotUnder, 2, 2));
        assertEquals("1.00", TdcFormat.getCoinInstance(US, 2, 2, 2).format(justNot));
        assertEquals("1.00", coinFormat.format(justNot, 2, 2, 2));
        assertEquals("0.999950", TdcFormat.getCoinInstance(US, 2, 2, 2).format(justNotUnder));
        assertEquals("0.999950", coinFormat.format(justNotUnder, 2, 2, 2));
        assertEquals("0.99999999", TdcFormat.getCoinInstance(US, 2, 2, 2, 2).format(justNot));
        assertEquals("0.99999999", coinFormat.format(justNot, 2, 2, 2, 2));
        assertEquals("0.99999999", TdcFormat.getCoinInstance(US, 2, REPEATING_DOUBLETS).format(justNot));
        assertEquals("0.99999999", coinFormat.format(justNot, 2, REPEATING_DOUBLETS));
        assertEquals("0.999950", TdcFormat.getCoinInstance(US, 2, 2, 2, 2).format(justNotUnder));
        assertEquals("0.999950", coinFormat.format(justNotUnder, 2, 2, 2, 2));
        assertEquals("0.999950", TdcFormat.getCoinInstance(US, 2, REPEATING_DOUBLETS).format(justNotUnder));
        assertEquals("0.999950", coinFormat.format(justNotUnder, 2, REPEATING_DOUBLETS));
        assertEquals("1.000", TdcFormat.getCoinInstance(US, 3).format(justNot));
        assertEquals("1.000", coinFormat.format(justNot, 3));
        assertEquals("1.0000", TdcFormat.getCoinInstance(US, 4).format(justNot));
        assertEquals("1.0000", coinFormat.format(justNot, 4));

        final Coin slightlyMore = COIN.add(SATOSHI);
        assertEquals("1", TdcFormat.getCoinInstance(US, 0).format(slightlyMore));
        assertEquals("1", coinFormat.format(slightlyMore, 0));
        assertEquals("1.0", TdcFormat.getCoinInstance(US, 1).format(slightlyMore));
        assertEquals("1.0", coinFormat.format(slightlyMore, 1));
        assertEquals("1.00", TdcFormat.getCoinInstance(US, 2, 2).format(slightlyMore));
        assertEquals("1.00", coinFormat.format(slightlyMore, 2, 2));
        assertEquals("1.00", TdcFormat.getCoinInstance(US, 2, 2, 2).format(slightlyMore));
        assertEquals("1.00", coinFormat.format(slightlyMore, 2, 2, 2));
        assertEquals("1.00000001", TdcFormat.getCoinInstance(US, 2, 2, 2, 2).format(slightlyMore));
        assertEquals("1.00000001", coinFormat.format(slightlyMore, 2, 2, 2, 2));
        assertEquals("1.00000001", TdcFormat.getCoinInstance(US, 2, REPEATING_DOUBLETS).format(slightlyMore));
        assertEquals("1.00000001", coinFormat.format(slightlyMore, 2, REPEATING_DOUBLETS));
        assertEquals("1.000", TdcFormat.getCoinInstance(US, 3).format(slightlyMore));
        assertEquals("1.000", coinFormat.format(slightlyMore, 3));
        assertEquals("1.0000", TdcFormat.getCoinInstance(US, 4).format(slightlyMore));
        assertEquals("1.0000", coinFormat.format(slightlyMore, 4));

        final Coin pivot = COIN.add(SATOSHI.multiply(5));
        assertEquals("1.00000005", TdcFormat.getCoinInstance(US, 8).format(pivot));
        assertEquals("1.00000005", coinFormat.format(pivot, 8));
        assertEquals("1.00000005", TdcFormat.getCoinInstance(US, 7, 1).format(pivot));
        assertEquals("1.00000005", coinFormat.format(pivot, 7, 1));
        assertEquals("1.0000001", TdcFormat.getCoinInstance(US, 7).format(pivot));
        assertEquals("1.0000001", coinFormat.format(pivot, 7));

        final Coin value = Coin.valueOf(1122334455667788l);
        assertEquals("11,223,345", TdcFormat.getCoinInstance(US, 0).format(value));
        assertEquals("11,223,345", coinFormat.format(value, 0));
        assertEquals("11,223,344.6", TdcFormat.getCoinInstance(US, 1).format(value));
        assertEquals("11,223,344.6", coinFormat.format(value, 1));
        assertEquals("11,223,344.5567", TdcFormat.getCoinInstance(US, 2, 2).format(value));
        assertEquals("11,223,344.5567", coinFormat.format(value, 2, 2));
        assertEquals("11,223,344.556678", TdcFormat.getCoinInstance(US, 2, 2, 2).format(value));
        assertEquals("11,223,344.556678", coinFormat.format(value, 2, 2, 2));
        assertEquals("11,223,344.55667788", TdcFormat.getCoinInstance(US, 2, 2, 2, 2).format(value));
        assertEquals("11,223,344.55667788", coinFormat.format(value, 2, 2, 2, 2));
        assertEquals("11,223,344.55667788", TdcFormat.getCoinInstance(US, 2, REPEATING_DOUBLETS).format(value));
        assertEquals("11,223,344.55667788", coinFormat.format(value, 2, REPEATING_DOUBLETS));
        assertEquals("11,223,344.557", TdcFormat.getCoinInstance(US, 3).format(value));
        assertEquals("11,223,344.557", coinFormat.format(value, 3));
        assertEquals("11,223,344.5567", TdcFormat.getCoinInstance(US, 4).format(value));
        assertEquals("11,223,344.5567", coinFormat.format(value, 4));

        TdcFormat megaFormat = TdcFormat.getInstance(-6, US);
        assertEquals("2,000.00", megaFormat.format(MAX_MONEY));
        assertEquals("2,000", megaFormat.format(MAX_MONEY, 0));
        assertEquals("11.22334455667788", megaFormat.format(value, 0, REPEATING_DOUBLETS));
        assertEquals("11.223344556677", megaFormat.format(Coin.valueOf(1122334455667700l), 0, REPEATING_DOUBLETS));
        assertEquals("11.22334455667788", megaFormat.format(value, 0, REPEATING_TRIPLETS));
        assertEquals("11.223344556677", megaFormat.format(Coin.valueOf(1122334455667700l), 0, REPEATING_TRIPLETS));
    }

    @Ignore("non-determinism between OpenJDK versions")
    @Test
    public void negativeTest() throws Exception {
        assertEquals("-1,00 TDC", TdcFormat.getInstance(FRANCE).format(COIN.multiply(-1)));
        assertEquals("TDC -1,00", TdcFormat.getInstance(ITALY).format(COIN.multiply(-1)));
        assertEquals("฿ -1,00", TdcFormat.getSymbolInstance(ITALY).format(COIN.multiply(-1)));
        assertEquals("TDC -1.00", TdcFormat.getInstance(JAPAN).format(COIN.multiply(-1)));
        assertEquals("฿-1.00", TdcFormat.getSymbolInstance(JAPAN).format(COIN.multiply(-1)));
        assertEquals("(TDC 1.00)", TdcFormat.getInstance(US).format(COIN.multiply(-1)));
        assertEquals("(฿1.00)", TdcFormat.getSymbolInstance(US).format(COIN.multiply(-1)));
        // assertEquals("TDC -१.००", TdcFormat.getInstance(Locale.forLanguageTag("hi-IN")).format(COIN.multiply(-1)));
        assertEquals("TDC -๑.๐๐", TdcFormat.getInstance(new Locale("th","TH","TH")).format(COIN.multiply(-1)));
        assertEquals("Ƀ-๑.๐๐", TdcFormat.getSymbolInstance(new Locale("th","TH","TH")).format(COIN.multiply(-1)));
    }

    /* Warning: these tests assume the state of Locale data extant on the platform on which
     * they were written: openjdk 7u21-2.3.9-5 */
    @Test
    public void equalityTest() throws Exception {
        // First, autodenominator
        assertEquals(TdcFormat.getInstance(), TdcFormat.getInstance());
        assertEquals(TdcFormat.getInstance().hashCode(), TdcFormat.getInstance().hashCode());

        assertNotEquals(TdcFormat.getCodeInstance(), TdcFormat.getSymbolInstance());
        assertNotEquals(TdcFormat.getCodeInstance().hashCode(), TdcFormat.getSymbolInstance().hashCode());

        assertEquals(TdcFormat.getSymbolInstance(5), TdcFormat.getSymbolInstance(5));
        assertEquals(TdcFormat.getSymbolInstance(5).hashCode(), TdcFormat.getSymbolInstance(5).hashCode());

        assertNotEquals(TdcFormat.getSymbolInstance(5), TdcFormat.getSymbolInstance(4));
        assertNotEquals(TdcFormat.getSymbolInstance(5).hashCode(), TdcFormat.getSymbolInstance(4).hashCode());

        /* The underlying formatter is mutable, and its currency code
         * and symbol may be reset each time a number is
         * formatted or parsed.  Here we check to make sure that state is
         * ignored when comparing for equality */
        // when formatting
        TdcAutoFormat a = (TdcAutoFormat)TdcFormat.getSymbolInstance(US);
        TdcAutoFormat b = (TdcAutoFormat)TdcFormat.getSymbolInstance(US);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        a.format(COIN.multiply(1000000));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        b.format(COIN.divide(1000000));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        // when parsing
        a = (TdcAutoFormat)TdcFormat.getSymbolInstance(US);
        b = (TdcAutoFormat)TdcFormat.getSymbolInstance(US);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        a.parseObject("mTDC2");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        b.parseObject("µ฿4.35");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

        // FRANCE and GERMANY have different pattterns
        assertNotEquals(TdcFormat.getInstance(FRANCE).hashCode(), TdcFormat.getInstance(GERMANY).hashCode());
        // TAIWAN and CHINA differ only in the Locale and Currency, i.e. the patterns and symbols are
        // all the same (after setting the currency symbols to tdcoins)
        assertNotEquals(TdcFormat.getInstance(TAIWAN), TdcFormat.getInstance(CHINA));
        // but they hash the same because of the DecimalFormatSymbols.hashCode() implementation

        assertEquals(TdcFormat.getSymbolInstance(4), TdcFormat.getSymbolInstance(4));
        assertEquals(TdcFormat.getSymbolInstance(4).hashCode(), TdcFormat.getSymbolInstance(4).hashCode());

        assertNotEquals(TdcFormat.getSymbolInstance(4), TdcFormat.getSymbolInstance(5));
        assertNotEquals(TdcFormat.getSymbolInstance(4).hashCode(), TdcFormat.getSymbolInstance(5).hashCode());

        // Fixed-denomination
        assertEquals(TdcFormat.getCoinInstance(), TdcFormat.getCoinInstance());
        assertEquals(TdcFormat.getCoinInstance().hashCode(), TdcFormat.getCoinInstance().hashCode());

        assertEquals(TdcFormat.getMilliInstance(), TdcFormat.getMilliInstance());
        assertEquals(TdcFormat.getMilliInstance().hashCode(), TdcFormat.getMilliInstance().hashCode());

        assertEquals(TdcFormat.getMicroInstance(), TdcFormat.getMicroInstance());
        assertEquals(TdcFormat.getMicroInstance().hashCode(), TdcFormat.getMicroInstance().hashCode());

        assertEquals(TdcFormat.getInstance(-6), TdcFormat.getInstance(-6));
        assertEquals(TdcFormat.getInstance(-6).hashCode(), TdcFormat.getInstance(-6).hashCode());

        assertNotEquals(TdcFormat.getCoinInstance(), TdcFormat.getMilliInstance());
        assertNotEquals(TdcFormat.getCoinInstance().hashCode(), TdcFormat.getMilliInstance().hashCode());

        assertNotEquals(TdcFormat.getCoinInstance(), TdcFormat.getMicroInstance());
        assertNotEquals(TdcFormat.getCoinInstance().hashCode(), TdcFormat.getMicroInstance().hashCode());

        assertNotEquals(TdcFormat.getMilliInstance(), TdcFormat.getMicroInstance());
        assertNotEquals(TdcFormat.getMilliInstance().hashCode(), TdcFormat.getMicroInstance().hashCode());

        assertNotEquals(TdcFormat.getInstance(SMALLEST_UNIT_EXPONENT),
                        TdcFormat.getInstance(SMALLEST_UNIT_EXPONENT - 1));
        assertNotEquals(TdcFormat.getInstance(SMALLEST_UNIT_EXPONENT).hashCode(),
                        TdcFormat.getInstance(SMALLEST_UNIT_EXPONENT - 1).hashCode());

        assertNotEquals(TdcFormat.getCoinInstance(TAIWAN), TdcFormat.getCoinInstance(CHINA));

        assertNotEquals(TdcFormat.getCoinInstance(2,3), TdcFormat.getCoinInstance(2,4));
        assertNotEquals(TdcFormat.getCoinInstance(2,3).hashCode(), TdcFormat.getCoinInstance(2,4).hashCode());

        assertNotEquals(TdcFormat.getCoinInstance(2,3), TdcFormat.getCoinInstance(2,3,3));
        assertNotEquals(TdcFormat.getCoinInstance(2,3).hashCode(), TdcFormat.getCoinInstance(2,3,3).hashCode());


    }

    @Ignore("non-determinism between OpenJDK versions")
    @Test
    public void attributeTest() throws Exception {
        String codePat = TdcFormat.getCodeInstance(Locale.US).pattern();
        assertTrue(codePat.contains("TDC") && ! codePat.contains("(^|[^฿])฿([^฿]|$)") && ! codePat.contains("(^|[^¤])¤([^¤]|$)"));
        String symPat = TdcFormat.getSymbolInstance(Locale.US).pattern();
        assertTrue(symPat.contains("฿") && !symPat.contains("TDC") && !symPat.contains("¤¤"));

        assertEquals("TDC #,##0.00;(TDC #,##0.00)", TdcFormat.getCodeInstance(Locale.US).pattern());
        assertEquals("฿#,##0.00;(฿#,##0.00)", TdcFormat.getSymbolInstance(Locale.US).pattern());
        assertEquals('0', TdcFormat.getInstance(Locale.US).symbols().getZeroDigit());
        // assertEquals('०', TdcFormat.getInstance(Locale.forLanguageTag("hi-IN")).symbols().getZeroDigit());
        // TODO will this next line work with other JREs?
        assertEquals('๐', TdcFormat.getInstance(new Locale("th","TH","TH")).symbols().getZeroDigit());
    }

    @Ignore("non-determinism between OpenJDK versions")
    @Test
    public void toStringTest() {
        assertEquals("Auto-format ฿#,##0.00;(฿#,##0.00)", TdcFormat.getSymbolInstance(Locale.US).toString());
        assertEquals("Auto-format ฿#,##0.0000;(฿#,##0.0000)", TdcFormat.getSymbolInstance(Locale.US, 4).toString());
        assertEquals("Auto-format TDC #,##0.00;(TDC #,##0.00)", TdcFormat.getCodeInstance(Locale.US).toString());
        assertEquals("Auto-format TDC #,##0.0000;(TDC #,##0.0000)", TdcFormat.getCodeInstance(Locale.US, 4).toString());
        assertEquals("Coin-format #,##0.00", TdcFormat.getCoinInstance(Locale.US).toString());
        assertEquals("Millicoin-format #,##0.00", TdcFormat.getMilliInstance(Locale.US).toString());
        assertEquals("Microcoin-format #,##0.00", TdcFormat.getMicroInstance(Locale.US).toString());
        assertEquals("Coin-format #,##0.000", TdcFormat.getCoinInstance(Locale.US,3).toString());
        assertEquals("Coin-format #,##0.000(####)(#######)", TdcFormat.getCoinInstance(Locale.US,3,4,7).toString());
        assertEquals("Kilocoin-format #,##0.000", TdcFormat.getInstance(-3,Locale.US,3).toString());
        assertEquals("Kilocoin-format #,##0.000(####)(#######)", TdcFormat.getInstance(-3,Locale.US,3,4,7).toString());
        assertEquals("Decicoin-format #,##0.000", TdcFormat.getInstance(1,Locale.US,3).toString());
        assertEquals("Decicoin-format #,##0.000(####)(#######)", TdcFormat.getInstance(1,Locale.US,3,4,7).toString());
        assertEquals("Dekacoin-format #,##0.000", TdcFormat.getInstance(-1,Locale.US,3).toString());
        assertEquals("Dekacoin-format #,##0.000(####)(#######)", TdcFormat.getInstance(-1,Locale.US,3,4,7).toString());
        assertEquals("Hectocoin-format #,##0.000", TdcFormat.getInstance(-2,Locale.US,3).toString());
        assertEquals("Hectocoin-format #,##0.000(####)(#######)", TdcFormat.getInstance(-2,Locale.US,3,4,7).toString());
        assertEquals("Megacoin-format #,##0.000", TdcFormat.getInstance(-6,Locale.US,3).toString());
        assertEquals("Megacoin-format #,##0.000(####)(#######)", TdcFormat.getInstance(-6,Locale.US,3,4,7).toString());
        assertEquals("Fixed (-4) format #,##0.000", TdcFormat.getInstance(-4,Locale.US,3).toString());
        assertEquals("Fixed (-4) format #,##0.000(####)", TdcFormat.getInstance(-4,Locale.US,3,4).toString());
        assertEquals("Fixed (-4) format #,##0.000(####)(#######)",
                     TdcFormat.getInstance(-4, Locale.US, 3, 4, 7).toString());

        assertEquals("Auto-format ฿#,##0.00;(฿#,##0.00)",
                     TdcFormat.builder().style(SYMBOL).code("USD").locale(US).build().toString());
        assertEquals("Auto-format #.##0,00 $",
                     TdcFormat.builder().style(SYMBOL).symbol("$").locale(GERMANY).build().toString());
        assertEquals("Auto-format #.##0,0000 $",
                     TdcFormat.builder().style(SYMBOL).symbol("$").fractionDigits(4).locale(GERMANY).build().toString());
        assertEquals("Auto-format TDC#,00฿;TDC-#,00฿",
                     TdcFormat.builder().style(SYMBOL).locale(GERMANY).pattern("¤¤#¤").build().toString());
        assertEquals("Coin-format TDC#,00฿;TDC-#,00฿",
                     TdcFormat.builder().scale(0).locale(GERMANY).pattern("¤¤#¤").build().toString());
        assertEquals("Millicoin-format TDC#.00฿;TDC-#.00฿",
                     TdcFormat.builder().scale(3).locale(US).pattern("¤¤#¤").build().toString());
    }

    @Test
    public void patternDecimalPlaces() {
        /* The pattern format provided by DecimalFormat includes specification of fractional digits,
         * but we ignore that because we have alternative mechanism for specifying that.. */
        TdcFormat f = TdcFormat.builder().locale(US).scale(3).pattern("¤¤ #.0").fractionDigits(3).build();
        assertEquals("Millicoin-format TDC #.000;TDC -#.000", f.toString());
        assertEquals("mTDC 1000.000", f.format(COIN));
    }

    @Ignore("non-determinism between OpenJDK versions")
    @Test
    public void builderTest() {
        Locale locale;
        if (Locale.getDefault().equals(GERMANY)) locale = FRANCE;
        else locale = GERMANY;

        assertEquals(TdcFormat.builder().build(), TdcFormat.getCoinInstance());
        try {
            TdcFormat.builder().scale(0).style(CODE);
            fail("Invoking both scale() and style() on a Builder should raise exception");
        } catch (IllegalStateException e) {}
        try {
            TdcFormat.builder().style(CODE).scale(0);
            fail("Invoking both style() and scale() on a Builder should raise exception");
        } catch (IllegalStateException e) {}

        TdcFormat built = TdcFormat.builder().style(TdcAutoFormat.Style.CODE).fractionDigits(4).build();
        assertEquals(built, TdcFormat.getCodeInstance(4));
        built = TdcFormat.builder().style(TdcAutoFormat.Style.SYMBOL).fractionDigits(4).build();
        assertEquals(built, TdcFormat.getSymbolInstance(4));

        built = TdcFormat.builder().scale(0).build();
        assertEquals(built, TdcFormat.getCoinInstance());
        built = TdcFormat.builder().scale(3).build();
        assertEquals(built, TdcFormat.getMilliInstance());
        built = TdcFormat.builder().scale(6).build();
        assertEquals(built, TdcFormat.getMicroInstance());

        built = TdcFormat.builder().locale(locale).scale(0).build();
        assertEquals(built, TdcFormat.getCoinInstance(locale));
        built = TdcFormat.builder().locale(locale).scale(3).build();
        assertEquals(built, TdcFormat.getMilliInstance(locale));
        built = TdcFormat.builder().locale(locale).scale(6).build();
        assertEquals(built, TdcFormat.getMicroInstance(locale));

        built = TdcFormat.builder().minimumFractionDigits(3).scale(0).build();
        assertEquals(built, TdcFormat.getCoinInstance(3));
        built = TdcFormat.builder().minimumFractionDigits(3).scale(3).build();
        assertEquals(built, TdcFormat.getMilliInstance(3));
        built = TdcFormat.builder().minimumFractionDigits(3).scale(6).build();
        assertEquals(built, TdcFormat.getMicroInstance(3));

        built = TdcFormat.builder().fractionGroups(3,4).scale(0).build();
        assertEquals(built, TdcFormat.getCoinInstance(2,3,4));
        built = TdcFormat.builder().fractionGroups(3,4).scale(3).build();
        assertEquals(built, TdcFormat.getMilliInstance(2,3,4));
        built = TdcFormat.builder().fractionGroups(3,4).scale(6).build();
        assertEquals(built, TdcFormat.getMicroInstance(2,3,4));

        built = TdcFormat.builder().pattern("#,####.#").scale(6).locale(GERMANY).build();
        assertEquals("100.0000,00", built.format(COIN));
        built = TdcFormat.builder().pattern("#,####.#").scale(6).locale(GERMANY).build();
        assertEquals("-100.0000,00", built.format(COIN.multiply(-1)));
        built = TdcFormat.builder().localizedPattern("#.####,#").scale(6).locale(GERMANY).build();
        assertEquals("100.0000,00", built.format(COIN));

        built = TdcFormat.builder().pattern("¤#,####.#").style(CODE).locale(GERMANY).build();
        assertEquals("฿-1,00", built.format(COIN.multiply(-1)));
        built = TdcFormat.builder().pattern("¤¤ #,####.#").style(SYMBOL).locale(GERMANY).build();
        assertEquals("TDC -1,00", built.format(COIN.multiply(-1)));
        built = TdcFormat.builder().pattern("¤¤##,###.#").scale(3).locale(US).build();
        assertEquals("mTDC1,000.00", built.format(COIN));
        built = TdcFormat.builder().pattern("¤ ##,###.#").scale(3).locale(US).build();
        assertEquals("₥฿ 1,000.00", built.format(COIN));

        try {
            TdcFormat.builder().pattern("¤¤##,###.#").scale(4).locale(US).build().format(COIN);
            fail("Pattern with currency sign and non-standard denomination should raise exception");
        } catch (IllegalStateException e) {}

        try {
            TdcFormat.builder().localizedPattern("¤¤##,###.#").scale(4).locale(US).build().format(COIN);
            fail("Localized pattern with currency sign and non-standard denomination should raise exception");
        } catch (IllegalStateException e) {}

        built = TdcFormat.builder().style(SYMBOL).symbol("B\u20e6").locale(US).build();
        assertEquals("B⃦1.00", built.format(COIN));
        built = TdcFormat.builder().style(CODE).code("XBT").locale(US).build();
        assertEquals("XBT 1.00", built.format(COIN));
        built = TdcFormat.builder().style(SYMBOL).symbol("$").locale(GERMANY).build();
        assertEquals("1,00 $", built.format(COIN));
        // Setting the currency code on a DecimalFormatSymbols object can affect the currency symbol.
        built = TdcFormat.builder().style(SYMBOL).code("USD").locale(US).build();
        assertEquals("฿1.00", built.format(COIN));

        built = TdcFormat.builder().style(SYMBOL).symbol("B\u20e6").locale(US).build();
        assertEquals("₥B⃦1.00", built.format(COIN.divide(1000)));
        built = TdcFormat.builder().style(CODE).code("XBT").locale(US).build();
        assertEquals("mXBT 1.00", built.format(COIN.divide(1000)));

        built = TdcFormat.builder().style(SYMBOL).symbol("B\u20e6").locale(US).build();
        assertEquals("µB⃦1.00", built.format(valueOf(100)));
        built = TdcFormat.builder().style(CODE).code("XBT").locale(US).build();
        assertEquals("µXBT 1.00", built.format(valueOf(100)));

        /* The prefix of a pattern can have number symbols in quotes.
         * Make sure our custom negative-subpattern creator handles this. */
        built = TdcFormat.builder().pattern("'#'¤#0").scale(0).locale(US).build();
        assertEquals("#฿-1.00", built.format(COIN.multiply(-1)));
        built = TdcFormat.builder().pattern("'#0'¤#0").scale(0).locale(US).build();
        assertEquals("#0฿-1.00", built.format(COIN.multiply(-1)));
        // this is an escaped quote between two hash marks in one set of quotes, not
        // two adjacent quote-enclosed hash-marks:
        built = TdcFormat.builder().pattern("'#''#'¤#0").scale(0).locale(US).build();
        assertEquals("#'#฿-1.00", built.format(COIN.multiply(-1)));
        built = TdcFormat.builder().pattern("'#0''#'¤#0").scale(0).locale(US).build();
        assertEquals("#0'#฿-1.00", built.format(COIN.multiply(-1)));
        built = TdcFormat.builder().pattern("'#0#'¤#0").scale(0).locale(US).build();
        assertEquals("#0#฿-1.00", built.format(COIN.multiply(-1)));
        built = TdcFormat.builder().pattern("'#0'E'#'¤#0").scale(0).locale(US).build();
        assertEquals("#0E#฿-1.00", built.format(COIN.multiply(-1)));
        built = TdcFormat.builder().pattern("E'#0''#'¤#0").scale(0).locale(US).build();
        assertEquals("E#0'#฿-1.00", built.format(COIN.multiply(-1)));
        built = TdcFormat.builder().pattern("E'#0#'¤#0").scale(0).locale(US).build();
        assertEquals("E#0#฿-1.00", built.format(COIN.multiply(-1)));
        built = TdcFormat.builder().pattern("E'#0''''#'¤#0").scale(0).locale(US).build();
        assertEquals("E#0''#฿-1.00", built.format(COIN.multiply(-1)));
        built = TdcFormat.builder().pattern("''#0").scale(0).locale(US).build();
        assertEquals("'-1.00", built.format(COIN.multiply(-1)));

        // immutability check for fixed-denomination formatters, w/ & w/o custom pattern
        TdcFormat a = TdcFormat.builder().scale(3).build();
        TdcFormat b = TdcFormat.builder().scale(3).build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        a.format(COIN.multiply(1000000));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        b.format(COIN.divide(1000000));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        a = TdcFormat.builder().scale(3).pattern("¤#.#").build();
        b = TdcFormat.builder().scale(3).pattern("¤#.#").build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        a.format(COIN.multiply(1000000));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        b.format(COIN.divide(1000000));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

    }

}
