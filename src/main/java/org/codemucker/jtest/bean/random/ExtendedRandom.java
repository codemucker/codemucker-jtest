/*
 * Copyright 2011 Bert van Brakel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codemucker.jtest.bean.random;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Random;

class ExtendedRandom extends Random {
char nextChar() {
    return (char) next(16);
}

byte nextByte() {
    return (byte) next(8);
}

short nextShort() {
    return (short) next(16);
}

BigDecimal nextBigDecimal() {
    int scale = nextInt();
    return new BigDecimal(nextBigInteger(), scale);
}

BigInteger nextBigInteger() {
    int randomLen = 1 + nextInt(15);
    byte[] bytes = new byte[randomLen];
    nextBytes(bytes);
    return new BigInteger(bytes);
}
}