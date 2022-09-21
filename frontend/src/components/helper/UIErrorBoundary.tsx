import CommonErrorBoundary from '@/components/helper/CommonErrorBoundary';
import {
	ErrorBoundaryProps,
	ErrorBoundaryState,
	UIErrorBoundaryProps,
} from '@/components/helper/types/ErrorBoundary.type';

class UIErrorBoundary extends CommonErrorBoundary<UIErrorBoundaryProps> {
	constructor(props: UIErrorBoundaryProps & ErrorBoundaryProps) {
		super(props);
	}

	componentDidUpdate(_: never, prevState: ErrorBoundaryState) {
		if (prevState.error === this.state.error || !this.state.error) {
			return;
		}

		if (prevState.error !== null) {
			this.reset();
		}
	}

	render() {
		const { children, serverErrorFallback, NotFoundErrorFallback } = this.props;

		if (this.state.error) {
			if (String(this.state.error.errorCode) === '0000') {
				return serverErrorFallback;
			}
			return NotFoundErrorFallback;
		}

		return children;
	}
}

export default UIErrorBoundary;
