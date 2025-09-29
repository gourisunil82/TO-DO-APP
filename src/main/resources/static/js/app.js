// app.js - Simplified version for your dashboard
console.log('Todo App JavaScript loaded');

document.addEventListener('DOMContentLoaded', function() {
    console.log('Initializing Todo App');
    
    const taskForm = document.getElementById('task-form');
    const submitBtn = document.getElementById('submit-btn');
    
    if (taskForm && submitBtn) {
        taskForm.addEventListener('submit', handleTaskSubmit);
        console.log('Task form event listener added');
    } else {
        console.log('Task form or submit button not found');
    }
});

async function handleTaskSubmit(event) {
    event.preventDefault(); // Prevent default form submission
    
    console.log('Form submitted via JavaScript');
    
    const titleInput = document.getElementById('title');
    const descriptionInput = document.getElementById('description');
    const submitBtn = document.getElementById('submit-btn');
    
    const title = titleInput.value.trim();
    const description = descriptionInput.value.trim();
    
    if (!title) {
        alert('Please enter a task title');
        return;
    }
    
    try {
        // Show processing state
        setProcessingState(true, submitBtn);
        
        console.log('Creating task:', { title, description });
        
        // Create task via API
        await createTask(title, description);
        
        console.log('Task created successfully');
        
        // Clear form
        titleInput.value = '';
        descriptionInput.value = '';
        
        // Show success message
        showAlert('Task created successfully!', 'success');
        
        // Reload the page to see the new task
        setTimeout(() => {
            window.location.reload();
        }, 1000);
        
    } catch (error) {
        console.error('Error creating task:', error);
        showAlert('Failed to create task: ' + error.message, 'error');
    } finally {
        setProcessingState(false, submitBtn);
    }
}

function setProcessingState(isProcessing, button) {
    if (!button) return;
    
    if (isProcessing) {
        button.disabled = true;
        button.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Processing...';
    } else {
        button.disabled = false;
        button.innerHTML = '<i class="bi bi-plus-lg me-1"></i>Add';
    }
}

async function createTask(title, description) {
    const formData = new URLSearchParams();
    formData.append('title', title);
    formData.append('description', description || '');

    const response = await fetch('/api/tasks', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        credentials: 'include',
        body: formData
    });
    
    console.log('API Response status:', response.status);
    
    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to create task');
    }
    
    return await response.json();
}

function showAlert(message, type) {
    // Remove existing alerts
    const existingAlert = document.querySelector('.custom-alert');
    if (existingAlert) {
        existingAlert.remove();
    }
    
    const alertClass = type === 'success' ? 'alert-success' : 'alert-danger';
    const alertHtml = `
        <div class="alert ${alertClass} alert-dismissible fade show custom-alert" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    
    // Insert at the top of the container
    const container = document.querySelector('.container');
    container.insertAdjacentHTML('afterbegin', alertHtml);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        const alert = document.querySelector('.custom-alert');
        if (alert) {
            alert.remove();
        }
    }, 5000);
}

// Add this to your existing animation script
document.addEventListener('DOMContentLoaded', function() {
    // Your existing animations...
    const taskItems = document.querySelectorAll('.task-item');
    taskItems.forEach(item => {
        item.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-2px)';
        });
        item.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0)';
        });
    });
});