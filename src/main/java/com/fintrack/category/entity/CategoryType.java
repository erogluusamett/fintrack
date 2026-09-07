package com.fintrack.category.entity;

/** Kategoriler yalnızca INCOME/EXPENSE olabilir — TRANSFER işlemler kategorisiz kalabilir (bkz. Transaction). */
public enum CategoryType {
    INCOME, EXPENSE
}
