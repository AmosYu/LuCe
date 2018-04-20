/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package luce.ctl.luce.BaiduMap;

import android.util.Log;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import luce.ctl.luce.utils.Point;

import static java.lang.Math.abs;

/**
 *
 * @author Administrator
 */
public class ConvexHull {

//    public static List<SH0001OutputBean> getConvexPoint(List<SH0001OutputBean> l) {
//        SH0001OutputBean[] array = new SH0001OutputBean[l.size()];
//        SH0001OutputBean[] convexPoint = getConvexPoint(l.toArray(array));
//        return Arrays.asList(convexPoint);
//    }

    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    private static int MAX_ANGLE = 4;
    private double currentMinAngle = 0;
    private List<Point> hullPointList;
    private List<Integer> indexList;
    private Point[] ps;

    public Point[] getPs() {
        return ps;
    }

    private int firstIndex;

    public int getFirstIndex() {
        return firstIndex;
    }

    public ConvexHull(List<Point> l){
        hullPointList = new LinkedList<Point>();
        indexList = new LinkedList<Integer>();
        Point[] array = new Point[l.size()];
        ps = l.toArray(array);

        firstIndex = getFirstPoint();
        addToHull(firstIndex);
    }
    public int getFirstPoint() {
        int minIndex = 0;
        for (int i = 1; i < ps.length; i++) {
            if (new BigDecimal(ps[i].getLat()).compareTo(new BigDecimal(ps[minIndex].getLat()))==-1) {
//            Double.valueOf(ps[i].getLat()) < Double.valueOf(ps[minIndex].getLat())
                minIndex = i;
            } else if ((new BigDecimal(ps[i].getLat()).compareTo(new BigDecimal(ps[minIndex].getLat()))==0)
                    && (new BigDecimal(ps[i].getLon()).compareTo(new BigDecimal(ps[minIndex].getLon()))==-1)) {
               //(Double.valueOf(ps[i].getLat())== Double.valueOf(ps[minIndex].getLat())) && (Double.valueOf(ps[i].getLon())< Double.valueOf(ps[minIndex].getLon()))
                minIndex = i;
            }
        }
        return minIndex;
    }


    private void initialize() {
    }

    private void addToHull(int index) {
        indexList.add(index);
        hullPointList.add(ps[index]);
    }

    public List<Point> calculateHull() {
        for (int i = getNextIndex(firstIndex); i != firstIndex; i = getNextIndex(i)) {
            addToHull(i);
        }
       return showHullPoints();
    }

    private List<Point> showHullPoints() {
        Iterator<Point> itPoint = hullPointList.iterator();
        Iterator<Integer> itIndex = indexList.iterator();
        Point p;
        int i;
        int index = 0;
        System.out.println("The hull points is: -> ");
        List<Point> res = new ArrayList<>();
        while (itPoint.hasNext()) {
            i = itIndex.next();
            p = itPoint.next();
            res.add(p);
            System.out.print(i + ":(" + p.getLon()+ "," + p.getLon()+ ")  ");
            index++;
            if (index % 10 == 0)
                System.out.println();
        }
        System.out.println();
        System.out.println("****************************************************************");
        System.out.println("The count of all hull points is " + index);
        return res;
    }

    public int getNextIndex(int currentIndex) {
        double minAngle = MAX_ANGLE;
        double pseudoAngle;
        int minIndex = 0;
        for (int i = 0; i < ps.length; i++) {
            if (i != currentIndex) {
                try{
                    pseudoAngle = getPseudoAngle(new BigDecimal(ps[i].getLon()).subtract(new BigDecimal(ps[currentIndex].getLon())).doubleValue(),
                            new BigDecimal(ps[i].getLat()).subtract(new BigDecimal(ps[currentIndex].getLat())).doubleValue());
                }catch (Exception e){
                    e.printStackTrace();
                    continue;
                }
                if (pseudoAngle >= currentMinAngle && pseudoAngle < minAngle) {
                    minAngle = pseudoAngle;
                    minIndex = i;
                } else if (pseudoAngle == minAngle){
                            if (abs(new BigDecimal(ps[i].getLon()).subtract(new BigDecimal(ps[currentIndex].getLon())).doubleValue())>
                                    abs(new BigDecimal(ps[minIndex].getLon()).subtract(new BigDecimal(ps[currentIndex].getLon())).doubleValue())||
                                    abs(new BigDecimal(ps[i].getLat()).subtract(new BigDecimal(ps[currentIndex].getLat())).doubleValue())>
                                            abs(new BigDecimal(ps[minIndex].getLat()).subtract(new BigDecimal(ps[currentIndex].getLat())).doubleValue())){
                                minIndex = i;
                            }
                }
            }

        }
        currentMinAngle = minAngle;
        return minIndex;
    }

    public double getPseudoAngle(double dx, double dy) {
        if (dx > 0 && dy >= 0)
            return dy / (dx + dy);
        if (dx <= 0 && dy > 0)
            return 1 + (abs(dx) / (abs(dx) + dy));
        if (dx < 0 && dy <= 0)
            return 2 + (dy / (dx + dy));
        if (dx >= 0 && dy < 0)
            return 3 + (dx / (dx + abs(dy)));
        throw new RuntimeException("Impossible");
    }

}
