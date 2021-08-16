package com.applecat;

/**
 * Mitrix3 3 * 3 的矩阵
 */
public class Matrix3 {

    double[] values;
    int row, col;

    public Matrix3(double[] values, int row, int col) {
        this.values = values;
        this.row = row;
        this.col = col;
    }

    /**
     * 1、当矩阵A的列数（column）等于矩阵B的行数（row）时，A与B可以相乘。 2、矩阵C的行数等于矩阵A的行数，C的列数等于B的列数。
     * 3、乘积C的第m行第n列的元素等于矩阵A的第m行的元素与矩阵B的第n列对应元素乘积之和。
     * 
     * @param other
     * @return
     */
    public Matrix3 multiply(Matrix3 other) {
        if (this.col != other.row) {
            return null;
        }
        Matrix3 res = new Matrix3(new double[this.row * other.col], this.row, other.col);
        for (int row = 0; row < res.row; row++) {
            for (int col = 0; col < res.col; col++) {
                for (int i = 0; i < this.col; i++) {
                    res.values[row * res.col + col] += values[row * this.col + i] * other.values[i * other.col + col];
                }
            }
        }
        return res;
    }

    public Vertex transform(Vertex in) {
        return new Vertex(in.x * values[0] + in.y * values[3] + in.z * values[6],
                in.x * values[1] + in.y * values[4] + in.z * values[7],
                in.x * values[2] + in.y * values[5] + in.z * values[8]);
    }
}
