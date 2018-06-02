package se.tink.backend.common.workers.activity.renderers.svg.charts;

import com.google.common.base.Objects;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.jfree.ui.TextAnchor;
import se.tink.backend.common.utils.TinkIconUtils;
import se.tink.backend.common.workers.activity.renderers.svg.Canvas;
import se.tink.backend.common.workers.activity.renderers.themes.ColorTypes;
import se.tink.backend.common.workers.activity.renderers.themes.Theme;
import se.tink.backend.core.Balance;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Currency;
import se.tink.backend.core.KVPair;
import se.tink.backend.core.TinkUserAgent;
import se.tink.backend.core.Transaction;
import se.tink.backend.utils.FontUtils.Fonts;
import se.tink.libraries.i18n.Catalog;

public class LeftToSpendTransactionOverlay extends LeftToSpendLineChartArea {

    private final List<Category> categories;
    private final Font font;
    private Font iconFont;
    private final Paint iconPaint;
    private final float iconSize = 13f;
    private final float lineMargin = 7;
    private final Paint paint;
    private final float radius = 10;
    private final float textMargin = 10;
    private final float textSize = 12f;
    
    private List<KVPair<Balance, Transaction>> balanceTransactionPairs;
    
    public LeftToSpendTransactionOverlay(Theme theme, Catalog catalog, Currency currency, Locale locale,
            Calendar calendar, List<Category> list, TinkUserAgent userAgent) {
        super(theme, catalog, currency, locale, calendar);
        
        this.categories = list;
        this.iconPaint = Theme.Colors.WHITE;
        this.paint = getTheme().getColor(ColorTypes.EXPENSES);
        this.font = theme.getLightFont().deriveFont(textSize);
        this.iconFont = Fonts.TINK_ICONS.deriveFont(iconSize);
    }
    
    @Override
    public void draw(Canvas canvas, Boolean v2) {
        super.draw(canvas, v2);

        drawTransactions(canvas, v2);
    }

    private int drawTransaction(KVPair<Balance, Transaction> kvpair, Canvas canvas, int lastX, Boolean v2) {
        int nextX = lastX + (int) (radius * 2);

        RectF bounds = getBounds(canvas);
        PointF point = getCurvePoint(kvpair.getKey(), bounds);
        float right = bounds.x + bounds.width;
        float x = Math.max(point.x, radius);

        Color circleColor;
        if (Objects.equal(CategoryTypes.INCOME, kvpair.getValue().getCategoryType())) {
            circleColor = getTheme().getColor(ColorTypes.INCOME);
        } else {
            circleColor = getTheme().getColor(ColorTypes.EXPENSES);
        }

        canvas.drawFilledCircle(x, point.y, radius, circleColor);

        float textY = (canvas.getBounds().y + textSize);
        float textX = Math.max(lastX, x - radius);

        if (!Objects.equal(CategoryTypes.INCOME, kvpair.getValue().getCategoryType())) {

            canvas.drawLine(x, point.y, textX + radius, textY + lineMargin, Theme.Strokes.SIMPLE_STROKE, paint);

            String description = kvpair.getValue().getDescription();

            // Truncate description
            if (description.length() > 15) {
                description = description.substring(0, 13) + "\u2026";
            }

            float endX = textX + canvas.getTextWidth(description, font);
            while (endX >= right && description.length() >= 2) {
                description = description.substring(0, description.length() - 2) + "\u2026";
                endX = textX + canvas.getTextWidth(description, font);
            }

            canvas.drawText(description, textX, textY, TextAnchor.BASELINE_LEFT, paint, font);

            nextX = (int) (textX + textMargin + canvas.getTextWidth(description, font));
        }

        TextAnchor anchor;
        char icon;

        float x_offset;
        float y_offset;

        if (v2) {
            icon = TinkIconUtils.getV2CategoryIcon(getCategory(kvpair.getValue().getCategoryId()));
            anchor = TextAnchor.CENTER_RIGHT;
            x_offset = (float) -1;
            y_offset = (float) -3;
            iconFont = iconFont.deriveFont(10f);
        } else {
            icon = TinkIconUtils.getV1CategoryIcon(getCategory(kvpair.getValue().getCategoryId()));
            anchor = TextAnchor.BASELINE_CENTER;
            x_offset = 0;
            y_offset = 0;
        }

        canvas.drawText(new String(new char[] {
           icon
        }), x + x_offset, (float) ((point.y + radius * 0.4) + y_offset), anchor, iconPaint, iconFont);

        return nextX;
    }

    private void drawTransactions(Canvas canvas, Boolean v2) {
        int lastX = 0;
        if (getBalanceTransactionPairs() != null) {
            Collections.sort(getBalanceTransactionPairs(),
                    (o1, o2) -> o1.getKey().getDate().compareTo(o2.getKey().getDate()));

            for (KVPair<Balance, Transaction> transaction : getBalanceTransactionPairs()) {
                lastX = drawTransaction(transaction, canvas, lastX, v2);
            }
        }
    }

    public List<KVPair<Balance, Transaction>> getBalanceTransactionPairs() {
        return balanceTransactionPairs;
    }

    @Override
    protected float getBottomPadding() {
        return radius;
    }

    private Category getCategory(String categoryId) {
        for (Category c : categories) {
            if (c.getId().equals(categoryId)) {
                return c;
            }
        }
        
        return null;
    }

    @Override
    protected float getTopPadding() {
        float padding = 0;

        padding += radius;
        padding += textMargin;
        padding += textSize * 1.5;

        return padding;
    }

    public void setBalanceTransactionPairs(List<KVPair<Balance, Transaction>> balanceTransactionPairs) {
        this.balanceTransactionPairs = balanceTransactionPairs;
    }
}
