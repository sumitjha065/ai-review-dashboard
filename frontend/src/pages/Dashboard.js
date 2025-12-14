import React from 'react';
import { Grid, Paper, Typography, Box, Chip } from '@mui/material';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';
import { Doughnut } from 'react-chartjs-2';

ChartJS.register(ArcElement, Tooltip, Legend);

const Dashboard = ({ data }) => {
    const { positiveCount, neutralCount, negativeCount, overallSummary, topProsJson, topConsJson } = data;

    const pros = topProsJson ? JSON.parse(topProsJson) : [];
    const cons = topConsJson ? JSON.parse(topConsJson) : [];

    const chartData = {
        labels: ['Positive', 'Neutral', 'Negative'],
        datasets: [
            {
                data: [positiveCount, neutralCount, negativeCount],
                backgroundColor: [
                    'rgba(75, 192, 192, 0.8)',
                    'rgba(255, 206, 86, 0.8)',
                    'rgba(255, 99, 132, 0.8)',
                ],
                borderColor: [
                    'rgba(75, 192, 192, 1)',
                    'rgba(255, 206, 86, 1)',
                    'rgba(255, 99, 132, 1)',
                ],
                borderWidth: 1,
            },
        ],
    };

    return (
        <Box>
            <Grid container spacing={3}>
                <Grid item xs={12} md={4}>
                    <Paper sx={{ p: 3, height: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                        <Typography variant="h6" gutterBottom>Sentiment Distribution</Typography>
                        <Box sx={{ maxWidth: 300, mx: 'auto' }}>
                            <Doughnut data={chartData} options={{
                                plugins: {
                                    legend: {
                                        labels: { color: '#f8fafc', font: { family: 'Poppins' } }
                                    }
                                }
                            }} />
                        </Box>
                    </Paper>
                </Grid>

                <Grid item xs={12} md={8}>
                    <Paper sx={{ p: 3, height: '100%' }}>
                        <Typography variant="h6" gutterBottom sx={{
                            background: 'linear-gradient(45deg, #6366f1, #ec4899)',
                            WebkitBackgroundClip: 'text',
                            WebkitTextFillColor: 'transparent',
                            fontWeight: 700
                        }}>
                            AI Analysis Summary
                        </Typography>

                        {(overallSummary.startsWith("FAILED") || overallSummary.startsWith("Could not generate")) ? (
                            <Box sx={{ p: 2, bgcolor: 'rgba(255, 0, 0, 0.1)', border: '1px solid #ef5350', borderRadius: 2 }}>
                                <Typography color="error" variant="body1" fontWeight="bold">
                                    Analysis Failed
                                </Typography>
                                <Typography color="error" variant="body2">
                                    {overallSummary}
                                </Typography>
                            </Box>
                        ) : (
                            <Typography variant="body1" paragraph sx={{ lineHeight: 1.8, color: 'text.secondary' }}>
                                {overallSummary}
                            </Typography>
                        )}

                        <Grid container spacing={2} sx={{ mt: 2 }}>
                            <Grid item xs={12} sm={6}>
                                <Typography variant="subtitle1" fontWeight="bold" color="success.main">Top Pros</Typography>
                                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mt: 1 }}>
                                    {pros.map((pro, index) => (
                                        <Chip key={index} label={pro} color="success" variant="outlined" />
                                    ))}
                                </Box>
                            </Grid>
                            <Grid item xs={12} sm={6}>
                                <Typography variant="subtitle1" fontWeight="bold" color="error.main">Top Cons</Typography>
                                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mt: 1 }}>
                                    {cons.map((con, index) => (
                                        <Chip key={index} label={con} color="error" variant="outlined" />
                                    ))}
                                </Box>
                            </Grid>
                        </Grid>
                    </Paper>
                </Grid>
            </Grid>
        </Box>
    );
};

export default Dashboard;
