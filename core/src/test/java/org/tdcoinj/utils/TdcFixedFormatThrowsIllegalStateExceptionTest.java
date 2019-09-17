/*
 * Copyright 2019 Tim Strasser
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Locale;

import org.junit.Test;

public class TdcFixedFormatThrowsIllegalStateExceptionTest {

    @Test(expected = IllegalStateException.class)
    public void testScaleThrowsIllegalStateException() {
        TdcFixedFormat tdcFixedFormat = new TdcFixedFormat(Locale.getDefault(), TdcFormat.MILLICOIN_SCALE, 2,
                new ArrayList<Integer>());
        tdcFixedFormat.scale(new BigInteger("2000"), 0);
    }
}
