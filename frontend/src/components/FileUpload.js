import React, { useCallback, useState } from 'react';
import { useDropzone } from 'react-dropzone';
import { Box, Typography, Button, LinearProgress, Alert } from '@mui/material';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import { uploadFile } from '../services/api';

const FileUpload = ({ onUploadSuccess }) => {
    const [uploading, setUploading] = useState(false);
    const [error, setError] = useState(null);

    const onDrop = useCallback(async (acceptedFiles) => {
        const file = acceptedFiles[0];
        if (!file) return;

        setUploading(true);
        setError(null);

        try {
            const result = await uploadFile(file);
            onUploadSuccess(result.batchId);
            setUploading(false); // Done uploading, parent handles loading analysis
        } catch (err) {
            setError("Failed to upload file. Please try again.");
            setUploading(false);
        }
    }, [onUploadSuccess]);

    const { getRootProps, getInputProps, isDragActive } = useDropzone({
        onDrop,
        accept: { 'text/csv': ['.csv'] },
        multiple: false
    });

    return (
        <Box sx={{ width: '100%' }}>
            <Box {...getRootProps()} sx={{
                border: '2px dashed',
                borderColor: isDragActive ? 'secondary.main' : 'primary.main',
                borderRadius: 4,
                p: 8,
                textAlign: 'center',
                cursor: 'pointer',
                bgcolor: isDragActive ? 'rgba(99, 102, 241, 0.1)' : 'transparent',
                transition: 'all 0.3s ease',
                '&:hover': {
                    bgcolor: 'rgba(99, 102, 241, 0.05)',
                    transform: 'scale(1.02)',
                    borderColor: 'secondary.main'
                }
            }}>
                <input {...getInputProps()} />
                <CloudUploadIcon sx={{ fontSize: 80, color: isDragActive ? 'secondary.main' : 'primary.main', mb: 3, opacity: 0.8 }} />
                <Typography variant="h5" gutterBottom fontWeight="600" color="text.primary">
                    {isDragActive ? "Drop to Analyze" : "Upload Review CSV"}
                </Typography>
                <Typography variant="body1" color="text.secondary">
                    Drag & drop or click to browse
                </Typography>
            </Box>

            {uploading && <LinearProgress sx={{ mt: 2 }} />}
            {error && <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>}
        </Box>
    );
};

export default FileUpload;
