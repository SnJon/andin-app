package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedErrorEvent
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.util.shareText
import ru.netology.nmedia.util.visibleOrGone
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private val binding: FragmentFeedBinding
        get() = _binding!!

    private val viewModel: PostViewModel by activityViewModels()

    private val postsAdapter by lazy { createAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindViews()
        subscribeOnViewModel()
    }

    private fun bindViews() {
        binding.apply {
            list.adapter = postsAdapter
            refresh.setOnRefreshListener {
                viewModel.showRecentPosts()
            }
            fab.setOnClickListener { findNavController().navigate(R.id.action_feedFragment_to_newPostFragment) }
            updateRecent.setOnClickListener {
                viewModel.showRecentPosts()
            }
        }
    }

    private fun subscribeOnViewModel() {
        viewModel.dbPostLiveData.observe(viewLifecycleOwner) { posts ->
            viewModel.updateContentState(posts)
        }
        viewModel.feedState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FeedModelState.Content -> showContent(state)
                FeedModelState.Loading -> showLoading()
                else -> Unit
            }
        }
        viewModel.feedErrorEvent.observe(viewLifecycleOwner) { errorState ->
            if (errorState == null) return@observe

            showError(errorState)
        }

        viewModel.newerCountLiveData.observe(viewLifecycleOwner) {
            viewModel.updateNewerCount(it)
        }

        viewModel.postLoadedEvent.observe(viewLifecycleOwner) {
            binding.refresh.isRefreshing = false
        }
    }

    private fun showError(errorState: FeedErrorEvent) {
        binding.refresh.isRefreshing = false
        if (errorState.itemIndex != null) {
            postsAdapter.refreshPost(errorState.itemIndex)
        }
        Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
            .setAction(R.string.retry_loading) { viewModel.loadPosts() }
            .show()
    }

    private fun showLoading() {
        binding.progress.isVisible = true
        binding.list.isVisible = false
    }

    private fun showContent(state: FeedModelState.Content) {
        binding.apply {
            refresh.isRefreshing = false
            progress.isVisible = false
            emptyText.visibleOrGone(state.isEmpty())
            list.visibleOrGone(state.isEmpty().not())
            updateRecent.visibleOrGone(state.newerCount > 0)

            if (state.isEmpty().not()) {
                postsAdapter.submitList(state.posts) {
                    if (state.isScrollToTopNeeded) {
                        list.scrollToPosition(0)
                    }
                }
            }
        }
    }

    private fun createAdapter(): PostsAdapter {
        return PostsAdapter(
            object : OnInteractionListener {
                override fun onEdit(post: Post) {
                    viewModel.edit(post)
                    findNavController().navigate(
                        R.id.action_feedFragment_to_newPostFragment,
                        Bundle().apply { textArg = post.content })
                }

                override fun onLike(post: Post) {
                    viewModel.onLikeClicked(post)
                }

                override fun onRemove(post: Post) {
                    viewModel.removeById(post.id)
                }

                override fun onShare(post: Post) {
                    context?.shareText(getString(R.string.chooser_share_post), post.content)
                }

                override fun onRetrySave(post: Post) {
                    viewModel.saveExist(post)
                }

                override fun onOpenImage(url: String) {
                    findNavController().navigate(R.id.action_feedFragment_to_imageFragment,
                        Bundle().apply { textArg = url })
                }
            }
        )
    }
}
