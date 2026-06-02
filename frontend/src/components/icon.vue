<template>
  <n-icon v-bind="props"><component :is="icon" /></n-icon>
</template>

<script lang="ts" setup>
import {
  AddOutline,
  AlertCircleOutline,
  BookOutline,
  BulbOutline,
  ConstructOutline,
  CubeOutline,
  DocumentTextOutline,
  FlaskOutline,
  FolderOpenOutline,
  HelpCircleOutline,
  KeyOutline,
  ListOutline,
  LogoGithub,
  NotificationsOutline,
  PeopleOutline,
  PersonCircleOutline,
  ReceiptOutline,
  RefreshOutline,
  SchoolOutline,
  SpeedometerOutline,
  MoonOutline
} from '@vicons/ionicons5'
import {
  AddRound as Add,
  ArrowForwardIosOutlined as ArrowForward,
  BarChartOutlined as BarChart,
  CategoryOutlined as Category,
  PeopleAltOutlined as People,
  PlayArrowOutlined as PlayArrow,
  QuizOutlined as Quiz,
  SchoolOutlined as School,
  SettingsOutlined as Settings,
  TrendingDownOutlined as TrendingDown,
  TrendingUpOutlined as TrendingUp,
  WbSunnyOutlined as WeatherSunny,
  AutoAwesomeOutlined as AutoAwesome,
  CheckCircleOutlined as CheckCircle,
  SaveOutlined as Save,
  DownloadOutlined as Download,
  DeleteForeverOutlined as DeleteForever,
  DocumentScannerOutlined as DocumentText
} from '@vicons/material'
import { NIcon } from 'naive-ui'

import GiteeIcon from './gitee-icon.vue'

const icons = {
  refresh: RefreshOutline,
  dashboard: SpeedometerOutline,
  product: CubeOutline,
  order: ReceiptOutline,
  users: PeopleOutline,
  role: PersonCircleOutline,
  category: FolderOpenOutline,
  permission: KeyOutline,
  flask: FlaskOutline,
  add: AddOutline,
  help: HelpCircleOutline,
  notifications: NotificationsOutline,
  github: LogoGithub,
  fallback: AlertCircleOutline,
  gitee: GiteeIcon,
  bulb: BulbOutline,
  list: ListOutline,
  knowledgePoint: BookOutline,
  school: SchoolOutline,
  usersOutline: PeopleOutline,
  documentText: DocumentTextOutline,
  construct: ConstructOutline,
  moon: MoonOutline,
  materialAdd: Add,
  materialArrowForward: ArrowForward,
  materialBarChart: BarChart,
  materialCategory: Category,
  materialPeople: People,
  materialPlayArrow: PlayArrow,
  materialQuiz: Quiz,
  materialSchool: School,
  materialSettings: Settings,
  materialTrendingDown: TrendingDown,
  materialTrendingUp: TrendingUp,
  materialWeatherSunny: WeatherSunny,
  materialAutoAwesome: AutoAwesome,
  materialCheckCircle: CheckCircle,
  materialSave: Save,
  materialDownload: Download,
  materialDeleteForever: DeleteForever,
  materialDocument: DocumentText
}

type IconTypes = keyof typeof icons

const props = defineProps({
  ...NIcon.props,
  type: {
    type: String,
    default: 'fallback'
  }
})

const icon = icons[props.type as IconTypes]
</script>
