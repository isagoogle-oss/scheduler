/**
 * マイページ用スクリプト（完全版）
 * ・円弧（最背面）
 * ・目盛り（中間）
 * ・予定名（最前面）
 * ・点線の目盛り維持
 * ・文字は白縁取り
 */

// 1. HTML側から渡されたサーバーサイドのデータを取得
const selectedWeekday = window.selectedWeekday;
const schedules = window.schedules;
const weekNames = window.weekNames;

// 2. 曜日切り替え
function changeDay() {
    const w = document.getElementById("weekSelect").value;
    window.location.href = `/mypage?week=${w}`;
}

// 3. 編集画面への遷移
function goEdit() {
    const w = document.getElementById("weekSelect").value;
    window.location.href = `/edit?week=${w}`;
}

/**
 * --- 時計風チャート用ロジック ---
 */
const TOTAL_MIN = 1440;

function toTimeStr(min) {
    const h = Math.floor(min / 60);
    const m = min % 60;
    return `${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}`;
}

const segments = schedules.map(s => ({
    label: s.categoryName,
    color: s.categoryColor,
    startMin: s.startTime,
    endMin: s.endTime,
    startAngle: (s.startTime / TOTAL_MIN) * 2 * Math.PI,
    endAngle: (s.endTime / TOTAL_MIN) * 2 * Math.PI
}));

document.addEventListener("DOMContentLoaded", () => {
    const tooltip = document.getElementById("tooltip");
    const ctxElement = document.getElementById("myChart");
    if (!ctxElement) return;

    const clockPlugin = {
        id: "clockPlugin",
        afterDraw(chart) {
            const { ctx, chartArea } = chart;
            const cx = (chartArea.left + chartArea.right) / 2;
            const cy = (chartArea.top + chartArea.bottom) / 2;
            const outerR = Math.min(chartArea.width, chartArea.height) / 2;
            const innerR = outerR * 0.5;

            ctx.save();

            /* ============================
             * ① 円弧（最背面）
             * ============================ */
            segments.forEach(seg => {
                const start = seg.startAngle - Math.PI / 2;
                const end = seg.endAngle - Math.PI / 2;

                ctx.beginPath();
                ctx.fillStyle = seg.color;
                ctx.arc(cx, cy, outerR, start, end);
                ctx.arc(cx, cy, innerR, end, start, true);
                ctx.closePath();
                ctx.fill();
            });

            /* ============================
             * ② 目盛り（点線＋数字）
             * ============================ */
            for (let h = 0; h < 24; h += 3) {
                const angle = (h / 24) * 2 * Math.PI - Math.PI / 2;

                const x1 = cx + Math.cos(angle) * (innerR - 5);
                const y1 = cy + Math.sin(angle) * (innerR - 5);
                const x2 = cx + Math.cos(angle) * (outerR + 5);
                const y2 = cy + Math.sin(angle) * (outerR + 5);

                // 点線の目盛り
                ctx.beginPath();
                ctx.setLineDash([8, 10]);
                ctx.strokeStyle = "white";
                ctx.lineWidth = 2;
                ctx.moveTo(x1, y1);
                ctx.lineTo(x2, y2);
                ctx.stroke();

                // 数字（縁取り）
                const labelR = outerR - 12;
                const lx = cx + Math.cos(angle) * labelR;
                const ly = cy + Math.sin(angle) * labelR;

                ctx.setLineDash([]);
                ctx.font = "bold 14px sans-serif";
                ctx.textAlign = "center";
                ctx.textBaseline = "middle";

                ctx.lineWidth = 4;
                ctx.strokeStyle = "white";
                ctx.strokeText(h.toString(), lx, ly);

                ctx.fillStyle = "black";
                ctx.fillText(h.toString(), lx, ly);
            }

            /* ============================
             * ③ 予定名（最前面）
             * ============================ */
            segments.forEach(seg => {
                if (seg.endMin - seg.startMin <= 30) return;

                const start = seg.startAngle - Math.PI / 2;
                const end = seg.endAngle - Math.PI / 2;
                const mid = (start + end) / 2;

                const textR = (outerR + innerR) / 2;
                const tx = cx + Math.cos(mid) * textR;
                const ty = cy + Math.sin(mid) * textR;

                ctx.font = "bold 13px sans-serif";
                ctx.textAlign = "center";
                ctx.textBaseline = "middle";

                ctx.lineWidth = 4;
                ctx.strokeStyle = "white";
                ctx.strokeText(seg.label, tx, ty);

                ctx.fillStyle = "black";
                ctx.fillText(seg.label, tx, ty);
            });

            ctx.restore();

            /* ============================
             * ④ ホバー判定
             * ============================ */
            const canvas = chart.canvas;
            canvas.onmousemove = (e) => {
                const rect = canvas.getBoundingClientRect();
                const x = e.clientX - rect.left - cx;
                const y = e.clientY - rect.top - cy;

                const dist = Math.sqrt(x * x + y * y);
                const angle = (Math.atan2(y, x) + Math.PI / 2 + 2 * Math.PI) % (2 * Math.PI);

                const found = segments.find(seg => {
                    if (dist < innerR || dist > outerR) return false;
                    return seg.startAngle <= seg.endAngle
                        ? (angle >= seg.startAngle && angle <= seg.endAngle)
                        : (angle >= seg.startAngle || angle <= seg.endAngle);
                });

                if (found) {
                    tooltip.style.display = "block";
                    tooltip.style.left = e.pageX + 15 + "px";
                    tooltip.style.top = e.pageY + 15 + "px";
                    tooltip.textContent =
                        `${found.label} (${toTimeStr(found.startMin)}〜${toTimeStr(found.endMin)})`;
                } else {
                    tooltip.style.display = "none";
                }
            };
        }
    };

    new Chart(ctxElement, {
        type: "doughnut",
        data: {
            datasets: [{
                data: [1],
                backgroundColor: ["#eee"],
                borderWidth: 0
            }]
        },
        options: {
            cutout: "50%",
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: { enabled: false }
            }
        },
        plugins: [clockPlugin]
    });
});
