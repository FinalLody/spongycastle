package org.bouncycastle.math.ec.custom.sec;

import java.math.BigInteger;

import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.Mod;
import org.bouncycastle.math.ec.Nat;
import org.bouncycastle.util.Arrays;

public class SecP224R1FieldElement extends ECFieldElement
{
    public static final BigInteger Q = SecP224R1Curve.q;

    protected int[] x;

    public SecP224R1FieldElement(BigInteger x)
    {
        if (x == null || x.signum() < 0 || x.compareTo(Q) >= 0)
        {
            throw new IllegalArgumentException("x value invalid for SecP224R1FieldElement");
        }

        this.x = SecP224R1Field.fromBigInteger(x);
    }

    public SecP224R1FieldElement()
    {
        this.x = Nat224.create();
    }

    protected SecP224R1FieldElement(int[] x)
    {
        this.x = x;
    }

    public boolean isZero()
    {
        return Nat224.isZero(x);
    }

    public boolean isOne()
    {
        return Nat224.isOne(x);
    }

    public boolean testBitZero()
    {
        return Nat224.getBit(x, 0) == 1;
    }

    public BigInteger toBigInteger()
    {
        return Nat224.toBigInteger(x);
    }

    public String getFieldName()
    {
        return "SecP224R1Field";
    }

    public int getFieldSize()
    {
        return Q.bitLength();
    }

    public ECFieldElement add(ECFieldElement b)
    {
        int[] z = Nat224.create();
        SecP224R1Field.add(x, ((SecP224R1FieldElement)b).x, z);
        return new SecP224R1FieldElement(z);
    }

    public ECFieldElement addOne()
    {
        int[] z = Nat224.create();
        SecP224R1Field.addOne(x, z);
        return new SecP224R1FieldElement(z);
    }

    public ECFieldElement subtract(ECFieldElement b)
    {
        int[] z = Nat224.create();
        SecP224R1Field.subtract(x, ((SecP224R1FieldElement)b).x, z);
        return new SecP224R1FieldElement(z);
    }

    public ECFieldElement multiply(ECFieldElement b)
    {
        int[] z = Nat224.create();
        SecP224R1Field.multiply(x, ((SecP224R1FieldElement)b).x, z);
        return new SecP224R1FieldElement(z);
    }

    public ECFieldElement divide(ECFieldElement b)
    {
//        return multiply(b.invert());
        int[] z = Nat224.create();
        Mod.invert(SecP224R1Field.P, ((SecP224R1FieldElement)b).x, z);
        SecP224R1Field.multiply(z, x, z);
        return new SecP224R1FieldElement(z);
    }

    public ECFieldElement negate()
    {
        int[] z = Nat224.create();
        SecP224R1Field.negate(x, z);
        return new SecP224R1FieldElement(z);
    }

    public ECFieldElement square()
    {
        int[] z = Nat224.create();
        SecP224R1Field.square(x, z);
        return new SecP224R1FieldElement(z);
    }

    public ECFieldElement invert()
    {
//        return new SecP224R1FieldElement(toBigInteger().modInverse(Q));
        int[] z = Nat224.create();
        Mod.invert(SecP224R1Field.P, x, z);
        return new SecP224R1FieldElement(z);
    }

    /**
     * return a sqrt root - the routine verifies that the calculation returns the right value - if
     * none exists it returns null.
     */
    public ECFieldElement sqrt()
    {
        int[] c = this.x;
        if (Nat224.isZero(c) || Nat224.isOne(c))
        {
            return this;
        }

        int[] d1 = Mod.random(SecP224R1Field.P);
        int[] e1 = Nat224.create();
        e1[0] = 1;

        int[] f = Nat224.create();
        RP(c, d1, e1, f);
        RS(d1, e1, f);

        int[] d0 = Nat224.create();
        int[] e0 = Nat224.create();

        for (int i = 0; i < 95; ++i)
        {
            Nat224.copy(d1, d0);
            Nat224.copy(e1, e0);

            RS(d1, e1, f);

            if (Nat224.isZero(d1))
            {
                break;
            }
        }

        Mod.invert(SecP224R1Field.P, e0, f);
        SecP224R1Field.multiply(f, d0, f);

        SecP224R1Field.square(f, d1);

        return Nat224.eq(c, d1) ? new SecP224R1FieldElement(f) : null;
    }

    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }

        if (!(other instanceof SecP224R1FieldElement))
        {
            return false;
        }

        SecP224R1FieldElement o = (SecP224R1FieldElement)other;
        return Nat224.eq(x, o.x);
    }

    public int hashCode()
    {
        return Q.hashCode() ^ Arrays.hashCode(x, 0, 7);
    }

    private static void RM(int[] c, int[] d0, int[] e0, int[] d1, int[] e1, int[] f)
    {
        int[] t = Nat224.create();
        SecP224R1Field.multiply(e1, e0, t);
        SecP224R1Field.multiply(t, c, t);
        SecP224R1Field.negate(t, t);
        SecP224R1Field.multiply(d1, d0, f);
        SecP224R1Field.add(f, t, f);
        SecP224R1Field.multiply(d1, e0, t);
        Nat224.copy(f, d1);
        SecP224R1Field.multiply(e1, d0, e1);
        SecP224R1Field.add(e1, t, e1);
        SecP224R1Field.square(e1, f);
        SecP224R1Field.multiply(f, c, f);
        SecP224R1Field.negate(f, f);
    }

    private static void RP(int[] c, int[] d1, int[] e1, int[] f)
    {
        SecP224R1Field.negate(c, f);

        int[] d0 = Nat224.create();
        int[] e0 = Nat224.create();

        for (int i = 0; i < 7; ++i)
        {
            Nat224.copy(d1, d0);
            Nat224.copy(e1, e0);

            int j = 1 << i;
            while (--j >= 0)
            {
                RS(d1, e1, f);
            }

            RM(c, d0, e0, d1, e1, f);
        }
    }

    private static void RS(int[] d, int[] e, int[] f)
    {
        SecP224R1Field.multiply(e, d, e);
        int[] t = Nat224.create();
        SecP224R1Field.square(d, t);
        SecP224R1Field.add(f, t, d);
        SecP224R1Field.twice(e, e);
        SecP224R1Field.multiply(f, t, f);
        int c = Nat.shiftUpBits(7, f, 2, 0);
        SecP224R1Field.reduce32(c, f);
    }
}