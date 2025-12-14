import React, { useState } from 'react';
import {
    Container,
    CssBaseline,
    ThemeProvider,
    createTheme,
    Box,
    Typography,
    Paper,
    CircularProgress,
    Grid,
    Chip
} from '@mui/material';

import FileUpload from './components/FileUpload';
import Dashboard from './pages/Dashboard';
import { getAnalysis } from './services/api';

// App theme configuration (dark mode)
const theme = createTheme({
    palette: {
        mode: 'dark',
        primary: {
            main: '#6366f1',
        },
        secondary: {
            main: '#ec4899',
        },
        background: {
            default: '#0f172a',
            paper: '#1e293b',
        },
        text: {
            primary: '#f8fafc',
            secondary: '#94a3b8',
        },
    },
    typography: {
        fontFamily: '"Poppins", "Roboto", "Helvetica", "Arial", sans-serif',
        h3: {
            fontWeight: 700,
            background: 'linear-gradient(45deg, #6366f1 30%, #ec4899 90%)',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
        },
        h6: {
            fontWeight: 400,
        }
    },
    components: {
        MuiPaper: {
            styleOverrides: {
                root: {
                    backgroundImage: 'none',
                    backgroundColor: 'rgba(30, 41, 59, 0.7)',
                },
            },
        },
        MuiButton: {
            styleOverrides: {
                root: {
                    borderRadius: '8px',
                    textTransform: 'none',
                },
            },
        },
    },
});

function App() {
    // Stores analysis response from backend
    const [analysisData, setAnalysisData] = useState(null);

    // Shows loading state while analysis is running
    const [loading, setLoading] = useState(false);

    // Stores error message (if any)
    const [error, setError] = useState(null);

    // Called after file upload is successful
    const handleAnalysisComplete = async (batchId) => {
        setLoading(true);
        setError(null);
        setAnalysisData(null);

        // Poll backend until analysis is completed
        const pollForResults = async (retries = 0) => {
            // Stop polling after some time
            if (retries > 300) {
                setError('Analysis timed out. Please try again later.');
                setLoading(false);
                return;
            }

            try {
                const response = await getAnalysis(batchId);

                // If analysis is still pending, keep polling
                if (response && response.status === 'PENDING') {
                    setTimeout(() => pollForResults(retries + 1), 2000);
                } else {
                    setAnalysisData(response);
                    setLoading(false);
                }
            } catch (error) {
                // Handle case when result is not ready yet
                if (error.response && error.response.status === 404) {
                    setTimeout(() => pollForResults(retries + 1), 2000);
                } else {
                    setError('Failed to fetch analysis.');
                    setLoading(false);
                }
            }
        };

        // Start polling
        pollForResults();
    };

    return (
        <ThemeProvider theme={theme}>
            <CssBaseline />
            <Box sx={{ minHeight: '100vh' }}>
                <Container maxWidth="lg" sx={{ pt: 8, pb: 8 }}>
                    {/* App heading */}
                    <Box sx={{ mb: 8, textAlign: 'center' }}>
                        <Typography variant="h3" gutterBottom>
                            Review Intelligence
                        </Typography>
                        <Typography variant="h6" color="text.secondary">
                            AI-powered sentiment analysis for customer reviews
                        </Typography>
                    </Box>

                    {/* File upload section */}
                    <Grid container justifyContent="center">
                        <Grid item xs={12} md={analysisData ? 12 : 8}>
                            <Paper sx={{ p: 6, textAlign: 'center' }}>
                                <FileUpload onUploadSuccess={handleAnalysisComplete} />
                            </Paper>
                        </Grid>
                    </Grid>

                    {/* Loading state */}
                    {loading && (
                        <Box sx={{ mt: 6, textAlign: 'center' }}>
                            <CircularProgress />
                            <Typography sx={{ mt: 2 }}>
                                Analyzing reviews...
                            </Typography>
                        </Box>
                    )}

                    {/* Error message */}
                    {error && (
                        <Box sx={{ mt: 4, textAlign: 'center' }}>
                            <Typography color="error">
                                {error}
                            </Typography>
                        </Box>
                    )}

                    {/* Dashboard output */}
                    {analysisData && !loading && (
                        <Box sx={{ mt: 6 }}>
                            <Dashboard data={analysisData} />
                        </Box>
                    )}
                </Container>
            </Box>
        </ThemeProvider>
    );
}

export default App;
