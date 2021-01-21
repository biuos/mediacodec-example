package dai.android.bili;

abstract class BaseRunnable implements Runnable {
    protected FMP4Save fmp4Save;

    protected BaseRunnable(FMP4Save _fmp4Save) {
        fmp4Save = _fmp4Save;
    }
}
