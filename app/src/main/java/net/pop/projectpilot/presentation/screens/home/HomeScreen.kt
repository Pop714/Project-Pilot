package net.pop.projectpilot.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.pop.projectpilot.data.firestore.Project

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToAllProjects: () -> Unit,
    onNavigateToProjectDetails: (Project) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.fetchHomeData(isRefresh = true) },
            modifier = Modifier.fillMaxSize()
        ) {
            when (val currentState = state) {
                is HomeState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is HomeState.Error -> {
                    HomeErrorState(
                        modifier = Modifier.align(Alignment.Center),
                        message = currentState.message,
                        fetchHomeData = viewModel::fetchHomeData,
                    )
                }

                is HomeState.Success -> {
                    HomeSuccessState(
                        currentState,
                        onNavigateToAllProjects,
                        onNavigateToProjectDetails
                    )
                }
            }
        }
    }
}