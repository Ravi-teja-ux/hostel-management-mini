package com.hostel.management.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "fees")
public class Fee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @Column(name = "total_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalFee;

    @Column(name = "paid_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal paidAmount;

    @Column(name = "pending_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal pendingAmount;

    @Column(name = "payment_status", nullable = false, length = 30)
    private String paymentStatus;

    protected Fee() {
    }

    public Fee(Student student, BigDecimal totalFee, BigDecimal paidAmount) {
        this.student = student;
        this.totalFee = totalFee;
        this.paidAmount = paidAmount;
        updatePaymentStatus();
    }

    @PrePersist
    @PreUpdate
    public void updatePaymentStatus() {
        if (totalFee != null && paidAmount != null) {
            pendingAmount = totalFee.subtract(paidAmount);
            paymentStatus = pendingAmount.signum() == 0 ? "Paid" : paidAmount.signum() == 0 ? "Pending" : "Partially Paid";
        }
    }

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public BigDecimal getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(BigDecimal totalFee) {
        this.totalFee = totalFee;
        updatePaymentStatus();
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
        updatePaymentStatus();
    }

    public BigDecimal getPendingAmount() {
        return pendingAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }
}
