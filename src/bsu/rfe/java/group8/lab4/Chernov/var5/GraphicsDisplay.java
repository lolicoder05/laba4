package bsu.rfe.java.group8.lab4.Chernov.var5;


import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GraphicsDisplay extends JPanel {

    // Список координат точек для построения графика
    private Double[][] graphicsData;

    // Флаговые переменные, задающие правила отображения графика
    private boolean showAxis = true;
    private boolean showMarkers = true;

    // Границы диапазона пространства, подлежащего отображению
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    // Используемый масштаб отображения
    private double scale;

    // Различные стили черчения линий
    private BasicStroke graphicsStroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;

    // Различные шрифты отображения надписей
    private Font axisFont;

    public GraphicsDisplay() {
        // Цвет заднего фона области отображения - белый
        setBackground(Color.WHITE);
        // Сконструировать необходимые объекты, используемые в рисовании
        // Перо для рисования графика
        graphicsStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND, 10.0f, null, 0.0f);
        // Перо для рисования осей координат
        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        // Перо для рисования контуров маркеров
        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        // Шрифт для подписей осей координат
        axisFont = new Font("Serif", Font.BOLD, 36);
    }

    // Данный метод вызывается из обработчика элемента меню "Открыть файл с графиком"
    // главного окна приложения в случае успешной загрузки данных
    public void showGraphics(Double[][] graphicsData) {
        // Сохранить массив точек во внутреннем поле класса
        this.graphicsData = graphicsData;
        // Запросить перерисовку компонента, т.е. неявно вызвать paintComponent()
        repaint();
    }

    // Методы-модификаторы для изменения параметров отображения графика
    // Изменение любого параметра приводит к перерисовке области
    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
        repaint();
    }

    // Метод отображения всего компонента, содержащего график
    public void paintComponent(Graphics g) {
        // Шаг 1: Вызвать метод предка для заливки области цветом заднего фона
        super.paintComponent(g);

        // Шаг 2: Если данные для графика не загружены, ничего не делать
        if (graphicsData == null || graphicsData.length == 0) return;

        // Шаг 3: Определить минимальные и максимальные значения координат X и Y
        minX = graphicsData[0][0];
        maxX = graphicsData[graphicsData.length - 1][0];
        minY = graphicsData[0][1];
        maxY = minY;

        // Найти минимальное и максимальное значение функции
        for (int i = 1; i < graphicsData.length; i++) {
            if (graphicsData[i][1] < minY) {
                minY = graphicsData[i][1];
            }
            if (graphicsData[i][1] > maxY) {
                maxY = graphicsData[i][1];
            }
        }

        // включение осей X=0 и Y=0 в видимую область
        if (minY > 0) minY = -10; // Если все точки выше оси X, включить ось X
        if (maxY < 0) maxY = 0; // Если все точки ниже оси X, включить ось X
        if (minX > 0) minX = 0; // Если все точки справа от оси Y, включить ось Y
        if (maxX < 0) maxX = 0; // Если все точки слева от оси Y, включить ось Y

        // Шаг 4: Определить масштабы по осям X и Y
        double scaleX = getSize().getWidth() / (maxX - minX);
        double scaleY = getSize().getHeight() / (maxY - minY);

        // Шаг 5: Чтобы изображение было неискажѐнным - масштаб должен быть одинаков
        scale = Math.min(scaleX, scaleY);

        // Шаг 6: Корректировка границ отображаемой области
        if (scale == scaleX) {
            double yIncrement = (getSize().getHeight() / scale - (maxY - minY)) / 2;
            maxY += yIncrement;
            minY -= yIncrement;
        }
        if (scale == scaleY) {
            double xIncrement = (getSize().getWidth() / scale - (maxX - minX)) / 2;
            maxX += xIncrement;
            minX -= xIncrement;
        }

        // Шаг 7: Сохранить текущие настройки холста
        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();

        // Шаг 8: В нужном порядке вызвать методы отображения элементов графика
        if (showAxis) paintAxis(canvas);
        paintGraphics(canvas);
        if (showMarkers) paintMarkers(canvas);

        // Шаг 9: Восстановить старые настройки холста
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
    }

    // Отрисовка графика по прочитанным координатам
    protected void paintGraphics(Graphics2D canvas) {
        // Устанавливаем стиль линии графика (чередование длинных и коротких отрезков)
        float[] dashPattern = {10, 10, 10,5, 5,5}; // Длина штрихов: 3 длинных -> 2 коротких
        canvas.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
        canvas.setColor(Color.red);

        GeneralPath graphics = new GeneralPath();
        for (int i = 0; i < graphicsData.length; i++) {
            Point2D.Double point = xyToPoint(graphicsData[i][0], graphicsData[i][1]);
            if (i > 0) {
                graphics.lineTo(point.getX(), point.getY());
            } else {
                graphics.moveTo(point.getX(), point.getY());
            }
        }
        canvas.draw(graphics);
    }


    // Отображение маркеров точек
    protected void paintMarkers(Graphics2D canvas) {
        canvas.setStroke(markerStroke);

        for (Double[] point : graphicsData) {
            Point2D.Double center = xyToPoint(point[0], point[1]);
            double x = point[0];
            double y = point[1];

            // Проверяем, идут ли цифры числа Y по возрастанию
            boolean isAscending = checkAscendingDigits(y);

            // Устанавливаем цвет маркера
            if (isAscending) {
                canvas.setColor(Color.GREEN); // Зеленый для точек, где цифры возрастают
            } else {
                canvas.setColor(Color.RED); // Красный для остальных точек
            }

            // Размер маркера (ромба)
            int markerSize = 11;

            // Центральные координаты маркера
            double cx = center.getX();
            double cy = center.getY();
            double halfSize = markerSize / 2.0;

            // Координаты вершин ромба
            Point2D.Double topLeft = new Point2D.Double(cx - halfSize, cy - halfSize);
            Point2D.Double topRight = new Point2D.Double(cx + halfSize, cy-halfSize);
            Point2D.Double bottomLeft = new Point2D.Double(cx - halfSize, cy + halfSize);
            Point2D.Double bottomRight = new Point2D.Double(cx + halfSize, cy + halfSize);

            //
            canvas.draw(new Line2D.Double(topLeft, topRight));
            canvas.draw(new Line2D.Double(topLeft, bottomLeft));
            canvas.draw(new Line2D.Double(bottomLeft, bottomRight));
            canvas.draw(new Line2D.Double(topRight, bottomRight));

            // Рисуем крест внутри ромба
            canvas.draw(new Line2D.Double(topLeft, bottomRight));
            canvas.draw(new Line2D.Double(bottomLeft, topRight));
        }
    }
    private boolean checkAscendingDigits(double value) {
        // Преобразуем число в строку
        String str = String.valueOf(Math.abs(value));

        // Переменная для хранения предыдущей цифры
        int previousDigit = -1;

        // Проходим по символам строки
        for (int i = 0; i < str.length(); i++) {
            char currentChar = str.charAt(i);

            // Проверяем, является ли текущий символ цифрой
            if (Character.isDigit(currentChar)) {
                int currentDigit = currentChar - '0'; // Преобразуем символ в цифру

                // Если порядок нарушен, возвращаем false
                if (previousDigit != -1 && currentDigit < previousDigit) {
                    return false;
                }

                // Обновляем предыдущую цифру
                previousDigit = currentDigit;
            }
        }

        return true; // Если все цифры идут по возрастанию, возвращаем true
    }


    // Метод, обеспечивающий отображение осей координат
    protected void paintAxis(Graphics2D canvas) {
        canvas.setStroke(axisStroke);
        canvas.setColor(Color.BLACK);
        canvas.setPaint(Color.BLACK);
        canvas.setFont(axisFont);

        FontRenderContext context = canvas.getFontRenderContext();

        // Ось Y
         {
            canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));

            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(0, maxY);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(lineEnd.getX() + 5, lineEnd.getY() + 20);
            arrow.lineTo(lineEnd.getX() - 5, lineEnd.getY() + 20);
            arrow.closePath();
            canvas.draw(arrow);
            canvas.fill(arrow);

            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, maxY);
            canvas.drawString("y", (float) labelPos.getX() + 10,
                    (float) (labelPos.getY() - bounds.getY()));
        }

        // Ось X (всегда видна)
        canvas.draw(new Line2D.Double(xyToPoint(minX, 0), xyToPoint(maxX, 0)));

        GeneralPath arrow = new GeneralPath();
        Point2D.Double lineEnd = xyToPoint(maxX, 0);
        arrow.moveTo(lineEnd.getX(), lineEnd.getY());
        arrow.lineTo(lineEnd.getX() - 20, lineEnd.getY() - 5);
        arrow.lineTo(lineEnd.getX() - 20, lineEnd.getY() + 5);
        arrow.closePath();
        canvas.draw(arrow);
        canvas.fill(arrow);

        Rectangle2D bounds = axisFont.getStringBounds("x", context);
        Point2D.Double labelPos = xyToPoint(maxX, 0);
        canvas.drawString("x", (float) (labelPos.getX() - bounds.getWidth() - 10),
                (float) (labelPos.getY() + bounds.getY()));
    }

    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX = x - minX;
        double deltaY = maxY - y;
        return new Point2D.Double(deltaX * scale, deltaY * scale);
    }

    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY) {
        return new Point2D.Double(src.getX() + deltaX, src.getY() + deltaY);
    }
}
