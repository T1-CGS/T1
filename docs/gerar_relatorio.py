"""Gera o PDF a partir da fonte Markdown. Dependencia opcional: reportlab."""
from pathlib import Path
from html import escape
import re

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.utils import ImageReader
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import Image, PageBreak, Paragraph, Preformatted, SimpleDocTemplate, Spacer, Table, TableStyle

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "docs/relatorio-final.md"
OUTPUT = ROOT / "output/pdf/relatorio-final-t1.pdf"
OUTPUT.parent.mkdir(parents=True, exist_ok=True)
FONT_DIR = Path("/System/Library/Fonts/Supplemental")
if (FONT_DIR / "Arial.ttf").exists():
    pdfmetrics.registerFont(TTFont("ReportBody", str(FONT_DIR / "Arial.ttf")))
    pdfmetrics.registerFont(TTFont("ReportBold", str(FONT_DIR / "Arial Bold.ttf")))
else:
    # Helvetica suporta os caracteres portugueses usados no relatorio.
    pdfmetrics.registerFont(pdfmetrics.Font("ReportBody", "Helvetica", "WinAnsiEncoding"))
    pdfmetrics.registerFont(pdfmetrics.Font("ReportBold", "Helvetica-Bold", "WinAnsiEncoding"))
pdfmetrics.registerFontFamily("ReportBody", normal="ReportBody", bold="ReportBold")

NAVY = colors.HexColor("#17334d")
TEAL = colors.HexColor("#166c75")
WIDTH = A4[0] - 100
styles = getSampleStyleSheet()
styles.add(ParagraphStyle("ReportText", fontName="ReportBody", fontSize=10, leading=15,
                          spaceAfter=9, textColor=NAVY))
styles.add(ParagraphStyle("ReportTitle", parent=styles["ReportText"], fontName="ReportBold",
                          fontSize=25, leading=32, alignment=TA_CENTER, spaceBefore=52, spaceAfter=30))
styles.add(ParagraphStyle("ReportHeading", parent=styles["ReportText"], fontName="ReportBold",
                          fontSize=15, leading=21, spaceBefore=12, spaceAfter=13, keepWithNext=True))
styles.add(ParagraphStyle("ReportSubheading", parent=styles["ReportText"], fontName="ReportBold",
                          fontSize=11, leading=16, spaceBefore=8, textColor=TEAL, keepWithNext=True))
styles.add(ParagraphStyle("ReportCell", parent=styles["ReportText"], fontSize=8.5, leading=12, spaceAfter=0))
styles.add(ParagraphStyle("ReportCode", fontName="Courier", fontSize=8, leading=11, spaceAfter=12))


def inline(text):
    text = escape(text)
    text = re.sub(r"\*\*(.*?)\*\*", r"<b>\1</b>", text)
    return re.sub(r"`([^`]+)`", r'<font name="Courier">\1</font>', text)


def table(rows):
    count = len(rows[0])
    widths = [WIDTH * .62, WIDTH * .38] if count == 2 else [WIDTH * .36, WIDTH * .34, WIDTH * .30]
    if count != len(widths):
        widths = [WIDTH / count] * count
    cells = [[Paragraph(inline(cell), styles["ReportCell"]) for cell in row] for row in rows]
    result = Table(cells, colWidths=widths, repeatRows=1, hAlign="LEFT")
    result.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), colors.HexColor("#e1edf1")),
        ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, colors.HexColor("#f5f8fa")]),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LINEBELOW", (0, 0), (-1, 0), .7, TEAL),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 7),
        ("TOPPADDING", (0, 0), (-1, -1), 7),
        ("LEFTPADDING", (0, 0), (-1, -1), 8),
        ("RIGHTPADDING", (0, 0), (-1, -1), 8),
    ]))
    return result


def footer(canvas, doc):
    canvas.saveState()
    canvas.setStrokeColor(colors.HexColor("#cad6df"))
    canvas.line(50, 42, A4[0] - 50, 42)
    canvas.setFont("ReportBody", 8)
    canvas.setFillColor(NAVY)
    canvas.drawString(50, 28, "T1 | Controle de aquisicoes | 02/10/2026")
    canvas.drawRightString(A4[0] - 50, 28, str(doc.page))
    canvas.restoreState()


lines = SOURCE.read_text().splitlines()
story = []
i = 0
while i < len(lines):
    line = lines[i].strip()
    if not line:
        i += 1
    elif line == "---":
        story.append(PageBreak())
        i += 1
    elif line.startswith("```"):
        code = []
        i += 1
        while i < len(lines) and not lines[i].startswith("```"):
            code.append(lines[i])
            i += 1
        story.append(Preformatted("\n".join(code), styles["ReportCode"], maxLineLength=93))
        i += 1
    elif line.startswith("|"):
        rows = []
        while i < len(lines) and lines[i].strip().startswith("|"):
            cells = [cell.strip() for cell in lines[i].strip().strip("|").split("|")]
            if not all(re.fullmatch(r":?-+:?", cell) for cell in cells):
                rows.append(cells)
            i += 1
        story.extend([table(rows), Spacer(1, 14)])
    elif line.startswith("!["):
        target = re.match(r"!\[.*?\]\((.*?)\)", line).group(1)
        path = SOURCE.parent / target
        w, h = ImageReader(str(path)).getSize()
        scale = min(WIDTH / w, 320 / h)
        story.extend([Image(str(path), width=w * scale, height=h * scale), Spacer(1, 10)])
        i += 1
    elif line.startswith("# "):
        story.append(Paragraph(inline(line[2:]), styles["ReportTitle"]))
        i += 1
    elif line.startswith("## "):
        story.append(Paragraph(inline(line[3:]), styles["ReportHeading"]))
        i += 1
    elif line.startswith("### "):
        story.append(Paragraph(inline(line[4:]), styles["ReportSubheading"]))
        i += 1
    elif line.startswith("- "):
        story.append(Paragraph("&#8226; " + inline(line[2:]), styles["ReportText"]))
        i += 1
    else:
        paragraph = [line]
        i += 1
        while i < len(lines) and lines[i].strip() and not lines[i].startswith(("#", "|", "```", "![", "- ")):
            if lines[i].strip() == "---":
                break
            paragraph.append(lines[i].strip())
            i += 1
        story.append(Paragraph(inline(" ".join(paragraph)), styles["ReportText"]))

SimpleDocTemplate(str(OUTPUT), pagesize=A4, rightMargin=50, leftMargin=50, topMargin=45,
                  bottomMargin=57, title="T1 - Relatorio final", author="Equipe T1")\
    .build(story, onFirstPage=footer, onLaterPages=footer)
print(OUTPUT)
