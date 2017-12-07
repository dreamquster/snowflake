package org.storm.core;

/**
 * Created by dknight on 2017/12/6.
 */
public class IdRange {
    private Long start;

    private Long end;

    private Integer step;

    public IdRange() {
        start = 0L;
        end = 0L;
        step = 0;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public boolean exceedMid(Long v) {
        return (v - start) >= (step / 2);
    }


}
