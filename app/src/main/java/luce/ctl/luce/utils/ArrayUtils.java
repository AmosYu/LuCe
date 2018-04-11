/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package luce.ctl.luce.utils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class ArrayUtils {

    public static byte[] append(byte[] array1, byte[] array2) {
        if (array1 == null && array2 == null) {
            return new byte[0];
        }
        if (array1 == null) {
            return array2;
        }
        if (array2 == null) {
            return array1;
        }
        byte[] data3 = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, data3, 0, array1.length);
        System.arraycopy(array2, 0, data3, array1.length, array2.length);
        return data3;
    }

    public static List<byte[]> split(byte[] array, byte[] split) {
        if (array == null) {
            return new ArrayList<>();
        }
        if (split.length > array.length) {
            return new ArrayList<>();
        }
        List<byte[]> res = new ArrayList<>();
        int index = -1;
        while ((index = indexOf(array, split)) != -1) {
            byte[] sub = subArray(array, 0, index);
            if (sub.length > 0) {
                res.add(sub);
            }
            array = subArray(array, index + split.length);

        }
        if (array.length > 0) {
            res.add(array);
        }
        return res;
    }

    public static byte[] subArray(byte[] array, int fromindex, int length) {
        if (array == null) {
            return new byte[0];
        }
        if (fromindex + length > array.length) {
            return new byte[0];
        }

        byte[] data3 = new byte[length];
        System.arraycopy(array, fromindex, data3, 0, length);
        return data3;
    }

    public static byte[] subArray(byte[] array, int fromindex) {
        if (array == null) {
            return new byte[0];
        }
        if (fromindex >= array.length) {
            return new byte[0];
        }

        byte[] data3 = new byte[array.length - fromindex];
        System.arraycopy(array, fromindex, data3, 0, array.length - fromindex);
        return data3;
    }

    public static int indexOf(byte[] src, byte[] dst) {
        for (int i = 0; i < src.length; i++) {
            if (src[i] == dst[0] && src.length - i >= dst.length) {
                boolean found = true;
                for (int j = 0; j < dst.length; j++) {
                    if (src[i + j] != dst[j]) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    return i;
                }
            }
        }
        return -1;
    }
}
